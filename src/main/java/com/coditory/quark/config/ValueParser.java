package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.coditory.quark.config.Preconditions.expectNonNull;
import static java.util.Objects.requireNonNull;

public interface ValueParser {
    @NotNull
    static <V> ValueParser forType(@NotNull Class<V> type, @NotNull Function<String, V> parser) {
        expectNonNull(type, "type");
        expectNonNull(parser, "parser");
        return new TypedValueParser<>(type, parser);
    }

    boolean isApplicable(@NotNull Class<?> type, String value);
    <T> T parse(@NotNull Class<T> type, String value);
}

class TypedValueParser<V> implements ValueParser {
    private final Class<V> type;
    private final Function<String, V> parser;

    public TypedValueParser(@NotNull Class<V> type, @NotNull Function<String, V> parser) {
        this.type = requireNonNull(type, "Expected non null type");
        this.parser = requireNonNull(parser, "Expected non null parser");
    }

    @Override
    public boolean isApplicable(@NotNull Class<?> type, String value) {
        return type.isAssignableFrom(this.type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T parse(@NotNull Class<T> type, String value) {
        return (T) parser.apply(value);
    }
}