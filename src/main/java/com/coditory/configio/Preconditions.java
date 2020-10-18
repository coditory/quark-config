package com.coditory.configio;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.BiFunction;

final class Preconditions {
    private Preconditions() {
        throw new IllegalStateException("Do not instantiate utility class");
    }

    @SafeVarargs
    static <T> T expectAll(T value, String message, BiFunction<T, String, T>... expects) {
        for (BiFunction<T, String, T> expect : expects) {
            expect.apply(value, message);
        }
        return value;
    }

    static <T> T expectNonNull(T value) {
        return expectNonNull(value, "Expected non null value");
    }

    static <T> T expectNonNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static String expectNonEmpty(String value) {
        return expectNonEmpty(value, "Expected non empty string. Got: " + value);
    }

    static String expectNonEmpty(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static <E> List<E> expectNonEmpty(List<E> list) {
        return expectNonEmpty(list, "Expected non empty list. Got: " + list);
    }

    static <E> List<E> expectNonEmpty(List<E> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return list;
    }

    static void expectStartsWith(Path path, Path prefix) {
        expectStartsWith(path, prefix, "Expected path " + path + " to start with " + prefix);
    }

    static void expectStartsWith(Path path, Path prefix, String message) {
        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException(message);
        }
    }

    static void expect(boolean valid, String message) {
        if (!valid) {
            throw new IllegalArgumentException(message);
        }
    }
}
