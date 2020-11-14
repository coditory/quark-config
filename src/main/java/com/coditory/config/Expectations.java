package com.coditory.config;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.String.format;

final class Expectations {
    private Expectations() {
        throw new IllegalStateException("Do not instantiate utility class");
    }

    public static void expect(boolean valid, String message, Object... args) {
        if (!valid) {
            throw new IllegalArgumentException(format(message, args));
        }
    }

    @SafeVarargs
    public static <T> T expectAll(T value, String name, BiFunction<T, String, T>... expects) {
        for (BiFunction<T, String, T> expect : expects) {
            expect.apply(value, name);
        }
        return value;
    }

    @SafeVarargs
    public static <T> T expectAll(T value, Function<T, T>... expects) {
        for (Function<T, T> expect : expects) {
            expect.apply(value);
        }
        return value;
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

    public static int expectPositive(int value) {
        return expectPositive(value, null);
    }

    public static int expectPositive(int value, String name) {
        if (value <= 0) {
            String message = message("Expected positive int", name, value);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static int expectNegative(int value) {
        return expectPositive(value, null);
    }

    public static int expectNegative(int value, String name) {
        if (value >= 0) {
            String message = message("Expected negative int", name, value);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static int expectNonNegative(int value) {
        return expectPositive(value, null);
    }

    public static int expectNonNegative(int value, String name) {
        if (value < 0) {
            String message = message("Expected non-negative int", name, value);
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
