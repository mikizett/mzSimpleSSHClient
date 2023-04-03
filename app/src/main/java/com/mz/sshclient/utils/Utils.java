package com.mz.sshclient.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

public final class Utils {

    private Utils() {}

    public static ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public static char[] encodeCharArrayAsCharArray(final char[] chars) {
        return encodeString(new String(chars)).toCharArray();
    }

    public static char[] encodeStringAsCharArray(final String s) {
        return encodeString(s).toCharArray();
    }

    public static char[] decodeCharArrayAsCharArray(final char[] chars) {
        return decodeString(new String(chars)).toCharArray();
    }
    public static char[] decodeStringAsCharArray(final String s) {
        return decodeString(s).toCharArray();
    }

    public static String encodeString(final String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeString(final String s) {
        return new String(Base64.getDecoder().decode(s));
    }

    public static LocalDateTime toDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli),
                ZoneId.systemDefault());
    }

}
