package com.coditory.config;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface ValueParser {
    static <V> ValueParser forType(Class<V> type, Function<String, V> parser) {
        return new TypedValueParser<>(type, parser);
    }

    boolean isApplicable(Class<?> type, String value);
    <T> T parse(Class<T> type, String value);
}

class TypedValueParser<V> implements ValueParser {
    private final Class<V> type;
    private final Function<String, V> parser;

    public TypedValueParser(Class<V> type, Function<String, V> parser) {
        this.type = requireNonNull(type, "Expected non null type");
        this.parser = requireNonNull(parser, "Expected non null parser");
    }

    @Override
    public boolean isApplicable(Class<?> type, String value) {
        return type.isAssignableFrom(this.type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T parse(Class<T> type, String value) {
        return (T) parser.apply(value);
    }
}