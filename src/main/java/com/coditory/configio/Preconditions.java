package com.coditory.configio;

import java.util.Collection;
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
        return expectNonNull(value, "value");
    }

    static <T> T expectNonNull(T value, String argument) {
        if (value == null) {
            throw new IllegalArgumentException("Expected non null " + argument);
        }
        return value;
    }

    static String expectNonBlank(String value) {
        return expectNonBlank(value, "value");
    }

    static String expectNonBlank(String value, String argument) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Expected non blank " + argument);
        }
        return value;
    }

    static <E, C extends Collection<E>> C expectNonEmpty(C collection) {
        return expectNonEmpty(collection, "collection");
    }

    static <E, C extends Collection<E>> C expectNonEmpty(C collection, String argument) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException("Expected non empty " + argument);
        }
        return collection;
    }

    static void expect(boolean valid, String message) {
        if (!valid) {
            throw new IllegalArgumentException(message);
        }
    }
}
