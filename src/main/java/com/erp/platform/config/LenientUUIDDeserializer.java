package com.erp.platform.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.UUID;

/**
 * Accepts any string for UUID fields — returns null instead of throwing when the
 * value is empty or not in standard 36-char UUID format.
 */
public class LenientUUIDDeserializer extends StdDeserializer<UUID> {

    public LenientUUIDDeserializer() {
        super(UUID.class);
    }

    @Override
    public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
