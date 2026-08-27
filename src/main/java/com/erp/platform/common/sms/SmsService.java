package com.erp.platform.common.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * SMS / WhatsApp sender. Provider-agnostic with a Twilio implementation.
 *
 * Configure via environment / application properties:
 *   sms.provider=TWILIO            (default NONE → messages are simulated/logged)
 *   sms.twilio.account-sid=ACxxxx
 *   sms.twilio.auth-token=xxxx
 *   sms.twilio.from=+1XXXXXXXXXX          (SMS sender number)
 *   sms.whatsapp.from=+14155238886        (WhatsApp-enabled number)
 *
 * When the provider is not configured, sends are logged and reported as successful so the
 * campaign flow works end-to-end in dev without a paid gateway.
 */
@Service
@Slf4j
public class SmsService {

    @Value("${sms.provider:NONE}")
    private String provider;

    @Value("${sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${sms.twilio.auth-token:}")
    private String authToken;

    @Value("${sms.twilio.from:}")
    private String smsFrom;

    @Value("${sms.whatsapp.from:}")
    private String whatsappFrom;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();

    public boolean sendSms(String to, String body) {
        return send(to, body, false);
    }

    public boolean sendWhatsApp(String to, String body) {
        return send(to, body, true);
    }

    private boolean configured() {
        return "TWILIO".equalsIgnoreCase(provider)
                && !accountSid.isBlank() && !authToken.isBlank();
    }

    private boolean send(String to, String body, boolean whatsApp) {
        if (to == null || to.isBlank()) return false;
        if (!configured()) {
            log.info("[{} SIMULATED] to {}: {}", whatsApp ? "WhatsApp" : "SMS", to, truncate(body));
            return true;
        }
        String from = whatsApp ? whatsappFrom : smsFrom;
        if (from == null || from.isBlank()) {
            log.warn("No {} 'from' number configured — skipping message to {}", whatsApp ? "WhatsApp" : "SMS", to);
            return false;
        }
        try {
            String fromAddr = whatsApp ? "whatsapp:" + from : from;
            String toAddr = whatsApp ? "whatsapp:" + to : to;
            String form = "From=" + enc(fromAddr) + "&To=" + enc(toAddr) + "&Body=" + enc(body == null ? "" : body);
            String auth = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(12))
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) return true;
            log.warn("Twilio {} send to {} failed ({}): {}", whatsApp ? "WhatsApp" : "SMS", to, resp.statusCode(), truncate(resp.body()));
            return false;
        } catch (Exception e) {
            log.warn("{} send to {} errored: {}", whatsApp ? "WhatsApp" : "SMS", to, e.getMessage());
            return false;
        }
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
    private static String truncate(String s) { return s == null ? "" : (s.length() > 120 ? s.substring(0, 120) + "…" : s); }
}
