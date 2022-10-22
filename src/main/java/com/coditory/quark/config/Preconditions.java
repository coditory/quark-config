package com.coditory.quark.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;

final class Preconditions {
    private Preconditions() {
        throw new IllegalStateException("Do not instantiate utility class");
    }

    public static void expect(boolean valid, String message, Object... args) {
        if (!valid) {
            throw new IllegalArgumentException(format(message, args));
        }
    }

    public static <T> List<T> expectUnique(List<T> value) {
        return expectUnique(value, null);
    }

    public static <T> List<T> expectUnique(List<T> values, String name) {
        Set<T> unique = new HashSet<>(values);
        if (unique.size() != values.size()) {
            String field = name != null ? (": " + name) : "";
            throw new IllegalArgumentException("Expected unique values" + field);
        }
        return values;
    }

    public static <T> T expectNonNull(T value) {
        return expectNonNull(value, null);
    }

    public static <T> T expectNonNull(T value, String name) {
        if (value == null) {
            String field = name != null ? (": " + name) : "";
            throw new IllegalArgumentException("Expected non-null value" + field);
        }
        return value;
    }

    public static String expectNonBlank(String value) {
        return expectNonBlank(value, null);
    }

    public static String expectNonBlank(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            String message = message("Expected non-blank string", name, value);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static <E, C extends Collection<E>> C expectNonEmpty(C collection) {
        return expectNonEmpty(collection, null);
    }

    public static <E, C extends Collection<E>> C expectNonEmpty(C value, String name) {
        if (value == null || value.isEmpty()) {
            String message = message("Expected non-empty collection", name, value);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static <K, V, M extends Map<K, V>> M expectNonEmpty(M value) {
        return expectNonEmpty(value, null);
    }

    public static <K, V, M extends Map<K, V>> M expectNonEmpty(M value, String name) {
        if (value == null || value.isEmpty()) {
            String message = message("Expected non-empty map", name, value);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String message(String expectation, String fieldName, Object value) {
        String field = fieldName != null ? (": " + fieldName) : "";
        String stringifiedValue = value instanceof String
                ? ("\"" + value + "\"")
                : Objects.toString(value);
        return expectation + field + ". Got: " + stringifiedValue;
    }
}
