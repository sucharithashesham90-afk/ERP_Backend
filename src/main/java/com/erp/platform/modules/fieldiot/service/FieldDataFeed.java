package com.erp.platform.modules.fieldiot.service;

import com.erp.platform.modules.fieldiot.entity.FieldPlot;
import com.erp.platform.modules.fieldiot.entity.FieldReading;
import com.erp.platform.modules.fieldiot.entity.FieldReading.ReadingKind;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Source of weather, soil and satellite observations for a field.
 *
 * Runs in one of two modes, reported honestly on every sync so nobody mistakes generated numbers
 * for measurements:
 *
 * <ul>
 *   <li>{@code SIMULATED} (default) — plausible readings derived deterministically from the
 *       field's coordinates and the date. Nothing leaves the network. The same field on the same
 *       day always produces the same values, so dashboards and demos are stable.</li>
 *   <li>{@code LIVE} — calls the configured provider. The shipped default is Open-Meteo, which
 *       needs no API key and returns weather plus shallow soil temperature and moisture.</li>
 * </ul>
 *
 * Satellite indices have no keyless provider, so NDVI/EVI stay simulated unless a scene provider
 * URL is configured. Set {@code FIELDIOT_MODE=LIVE} to switch feeds on.
 */
@Component
@Slf4j
public class FieldDataFeed {

    private static final String SIMULATED_SOURCE = "SIMULATED";

    @Value("${fieldiot.mode:SIMULATED}")
    private String mode;

    @Value("${fieldiot.weather.url:https://api.open-meteo.com/v1/forecast}")
    private String weatherUrl;

    /** Optional satellite/NDVI provider. Expected to accept lat/lon and return an ndvi field. */
    @Value("${fieldiot.satellite.url:}")
    private String satelliteUrl;

    @Value("${fieldiot.satellite.apiKey:}")
    private String satelliteApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean isLive() {
        return "LIVE".equalsIgnoreCase(mode);
    }

    public String providerStatus() {
        if (!isLive()) return "Simulated — set FIELDIOT_MODE=LIVE to pull from a provider";
        StringBuilder s = new StringBuilder("Live: weather via ").append(hostOf(weatherUrl));
        s.append(satelliteUrl == null || satelliteUrl.isBlank()
                ? "; satellite simulated (no provider configured)"
                : "; satellite via " + hostOf(satelliteUrl));
        return s.toString();
    }

    /**
     * Readings for one field, one per kind. A live call that fails falls back to simulation for
     * that field rather than aborting the whole sync — a partial dashboard beats a broken one.
     */
    public List<FieldReading> fetch(FieldPlot field, LocalDateTime observedAt, List<String> messages) {
        List<FieldReading> out = new ArrayList<>();

        boolean coordinatesKnown = field.getCentroidLatitude() != null && field.getCentroidLongitude() != null;
        if (isLive() && !coordinatesKnown) {
            messages.add(field.getName() + ": no coordinates set — simulated instead of pulled");
        }

        JsonNode live = null;
        if (isLive() && coordinatesKnown) {
            live = getJson(weatherUrl
                    + "?latitude=" + field.getCentroidLatitude()
                    + "&longitude=" + field.getCentroidLongitude()
                    + "&current=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m"
                    + "&hourly=soil_temperature_0cm,soil_moisture_0_to_1cm"
                    + "&forecast_days=1", null);
            if (live == null) {
                messages.add(field.getName() + ": provider call failed — simulated for this field");
            }
        }

        out.add(live != null ? weatherFromProvider(field, observedAt, live)
                             : simulatedWeather(field, observedAt));
        out.add(live != null ? soilFromProvider(field, observedAt, live)
                             : simulatedSoil(field, observedAt));
        out.add(satellite(field, observedAt, messages));
        return out;
    }

    // ── Live provider mapping ────────────────────────────────────────────────

    private FieldReading weatherFromProvider(FieldPlot field, LocalDateTime at, JsonNode root) {
        FieldReading r = base(field, at, ReadingKind.WEATHER, hostOf(weatherUrl));
        JsonNode cur = root.path("current");
        r.setTemperatureC(decimal(cur.path("temperature_2m")));
        r.setHumidityPercent(decimal(cur.path("relative_humidity_2m")));
        r.setRainfallMm(decimal(cur.path("precipitation")));
        r.setWindSpeedKph(decimal(cur.path("wind_speed_10m")));
        r.setWeatherSummary(summarise(r.getRainfallMm(), r.getTemperatureC()));
        return r;
    }

    private FieldReading soilFromProvider(FieldPlot field, LocalDateTime at, JsonNode root) {
        FieldReading r = base(field, at, ReadingKind.SOIL, hostOf(weatherUrl));
        JsonNode hourly = root.path("hourly");
        r.setSoilTemperatureC(firstOf(hourly.path("soil_temperature_0cm")));
        // The provider reports volumetric water content (m³/m³); the dashboard wants a percentage.
        BigDecimal vwc = firstOf(hourly.path("soil_moisture_0_to_1cm"));
        r.setSoilMoisturePercent(vwc != null
                ? vwc.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : null);
        // Chemistry is not something a weather API measures — that comes from a probe or lab.
        FieldReading simulated = simulatedSoil(field, at);
        r.setSoilPh(simulated.getSoilPh());
        r.setSoilEc(simulated.getSoilEc());
        r.setNitrogenPpm(simulated.getNitrogenPpm());
        r.setPhosphorusPpm(simulated.getPhosphorusPpm());
        r.setPotassiumPpm(simulated.getPotassiumPpm());
        if (r.getSoilMoisturePercent() == null) r.setSoilMoisturePercent(simulated.getSoilMoisturePercent());
        return r;
    }

    private FieldReading satellite(FieldPlot field, LocalDateTime at, List<String> messages) {
        if (isLive() && satelliteUrl != null && !satelliteUrl.isBlank()
                && field.getCentroidLatitude() != null) {
            JsonNode root = getJson(satelliteUrl
                    + "?lat=" + field.getCentroidLatitude()
                    + "&lon=" + field.getCentroidLongitude(), satelliteApiKey);
            if (root != null) {
                FieldReading r = base(field, at, ReadingKind.SATELLITE, hostOf(satelliteUrl));
                r.setNdvi(decimal(root.path("ndvi")));
                r.setEvi(decimal(root.path("evi")));
                r.setCloudCoverPercent(decimal(root.path("cloudCover")));
                r.setSceneId(root.path("sceneId").asText(null));
                if (r.getNdvi() != null) return r;
            }
            messages.add(field.getName() + ": satellite provider returned no index — simulated");
        }
        return simulatedSatellite(field, at);
    }

    // ── Simulation ───────────────────────────────────────────────────────────
    //
    // Values are derived from a hash of the field id and the date, so they are stable for a given
    // field on a given day and vary sensibly between fields — a dashboard that reshuffles on every
    // refresh is worse than useless for spotting a trend.

    private FieldReading simulatedWeather(FieldPlot field, LocalDateTime at) {
        FieldReading r = base(field, at, ReadingKind.WEATHER, SIMULATED_SOURCE);
        long seed = seed(field, at, "w");
        r.setTemperatureC(scaled(seed, 18, 38, 1));
        r.setHumidityPercent(scaled(seed >> 8, 35, 92, 1));
        r.setRainfallMm(((seed >> 16) % 5 == 0) ? scaled(seed >> 20, 1, 45, 1) : BigDecimal.ZERO);
        r.setWindSpeedKph(scaled(seed >> 24, 2, 28, 1));
        r.setWeatherSummary(summarise(r.getRainfallMm(), r.getTemperatureC()));
        return r;
    }

    private FieldReading simulatedSoil(FieldPlot field, LocalDateTime at) {
        FieldReading r = base(field, at, ReadingKind.SOIL, SIMULATED_SOURCE);
        long seed = seed(field, at, "s");
        r.setSoilMoisturePercent(scaled(seed, 12, 45, 1));
        r.setSoilTemperatureC(scaled(seed >> 6, 16, 34, 1));
        r.setSoilPh(scaled(seed >> 12, 55, 85, 10));          // 5.5 – 8.5
        r.setSoilEc(scaled(seed >> 18, 2, 18, 10));           // 0.2 – 1.8 dS/m
        r.setNitrogenPpm(scaled(seed >> 24, 80, 320, 1));
        r.setPhosphorusPpm(scaled(seed >> 30, 8, 60, 1));
        r.setPotassiumPpm(scaled(seed >> 36, 90, 400, 1));
        return r;
    }

    private FieldReading simulatedSatellite(FieldPlot field, LocalDateTime at) {
        FieldReading r = base(field, at, ReadingKind.SATELLITE, SIMULATED_SOURCE);
        long seed = seed(field, at, "n");
        r.setNdvi(scaled(seed, 220, 850, 1000));              // 0.22 – 0.85
        r.setEvi(scaled(seed >> 10, 180, 720, 1000));
        r.setCloudCoverPercent(scaled(seed >> 20, 0, 60, 1));
        r.setSceneId("SIM-" + at.toLocalDate() + "-" + Math.abs(seed % 100000));
        return r;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private FieldReading base(FieldPlot field, LocalDateTime at, ReadingKind kind, String source) {
        FieldReading r = new FieldReading();
        r.setTenantId(field.getTenantId());
        r.setKind(kind);
        r.setFieldPlotId(field.getId());
        r.setFieldPlotName(field.getName());
        r.setObservedAt(at);
        r.setSource(source);
        return r;
    }

    private JsonNode getJson(String url, String apiKey) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET();
            if (apiKey != null && !apiKey.isBlank()) b.header("Authorization", "Bearer " + apiKey);
            HttpResponse<String> resp = httpClient.send(b.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                log.warn("Field data provider returned HTTP {} for {}", resp.statusCode(), hostOf(url));
                return null;
            }
            return objectMapper.readTree(resp.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("Field data provider call failed for {}: {}", hostOf(url), e.getMessage());
            return null;
        }
    }

    private static BigDecimal decimal(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull()
                ? null : BigDecimal.valueOf(node.asDouble()).setScale(3, RoundingMode.HALF_UP);
    }

    private static BigDecimal firstOf(JsonNode array) {
        if (array == null || !array.isArray() || array.isEmpty()) return null;
        return decimal(array.get(0));
    }

    /** Deterministic per field, per day, per feed. */
    private static long seed(FieldPlot field, LocalDateTime at, String salt) {
        String key = (field.getId() != null ? field.getId().toString() : field.getName())
                + "|" + at.toLocalDate() + "|" + salt;
        return Math.abs((long) key.hashCode()) * 2654435761L;
    }

    /** Maps a seed into [min,max] then divides by {@code divisor} to place the decimal point. */
    private static BigDecimal scaled(long seed, int min, int max, int divisor) {
        long span = Math.max(1, max - min);
        long raw = min + Math.floorMod(seed, span);
        return BigDecimal.valueOf(raw).divide(BigDecimal.valueOf(divisor), 3, RoundingMode.HALF_UP);
    }

    private static String summarise(BigDecimal rainfall, BigDecimal temperature) {
        if (rainfall != null && rainfall.compareTo(BigDecimal.valueOf(10)) > 0) return "Heavy rain";
        if (rainfall != null && rainfall.compareTo(BigDecimal.ZERO) > 0) return "Light rain";
        if (temperature != null && temperature.compareTo(BigDecimal.valueOf(35)) > 0) return "Hot and dry";
        return "Clear";
    }

    private static String hostOf(String url) {
        try { return URI.create(url).getHost(); } catch (Exception e) { return url; }
    }
}
