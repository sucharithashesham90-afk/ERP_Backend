package com.erp.platform.common.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Null-safe extractors for {@code Map<String,Object>} request bodies.
 * Used by the flexible document controllers (inventory issues/receipts, intakes, etc.)
 * so JSON field names round-trip exactly as the frontend sends them.
 */
public final class PayloadUtils {

    private PayloadUtils() {}

    public static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    public static boolean bool(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    /** Parses a UUID from a String; returns null for null/blank/non-UUID (e.g. NaN from a bad frontend cast). */
    public static UUID uuid(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s) || "NaN".equalsIgnoreCase(s)) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static LocalDate date(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s);
        } catch (Exception ex) {
            return null;
        }
    }

    public static BigDecimal decimal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        if (s.isEmpty()) return null;
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
