package com.coditory.quark.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coditory.quark.config.Preconditions.expectNonNull;
import static java.util.stream.Collectors.toList;

class ConfigValue {
    private final Path path;
    private final Object value;

    ConfigValue(Path path, Object value) {
        this.path = expectNonNull(path);
        this.value = expectNonNull(value);
    }

    @SuppressWarnings("unchecked")
    <T> T getAs(ConfigValueParser valueParser, Class<T> type) {
        if (Config.class.equals(type)) {
            return (T) getAsConfig();
        }
        if (String.class.equals(type)) {
            return (T) getAsString();
        }
        if (Object.class.equals(type)) {
            return (T) value;
        }
        return getOrParse(valueParser, type);
    }

    @SuppressWarnings("unchecked")
    <T> List<T> getAsList(ConfigValueParser valueParser, Class<T> type) {
        if (!(value instanceof List)) {
            throw new ConfigValueConversionException(List.class, path.toString(), value);
        }
        List<Object> rawValues = (List<Object>) value;
        List<ConfigValue> values = new ArrayList<>();
        for (int i = 0; i < rawValues.size(); ++i) {
            Object rawValue = rawValues.get(i);
            values.add(new ConfigValue(path.add(i), rawValue));
        }
        return values.stream()
                .map(v -> v.getAs(valueParser, type))
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    Config getAsConfig() {
        if (value instanceof Map) {
            Map<String, Object> subConfig = (Map<String, Object>) value;
            return Config.of(subConfig);
        }
        if (value instanceof Config) {
            return (Config) value;
        }
        throw new ConfigValueConversionException(Config.class, path.toString(), value);
    }

    String getAsString() {
        if (!(value instanceof String)) {
            throw new ConfigValueConversionException(String.class, path.toString(), value);
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrParse(ConfigValueParser parser, Class<T> type) {
        if (value == null) {
            return null;
        }
        if (type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        if (value instanceof String) {
            return parse(parser, type, (String) value);
        }
        throw new ConfigValueConversionException(type, path.toString(), value);
    }

    private <T> T parse(ConfigValueParser parser, Class<T> type, String value) {
        try {
            return parser.parse(type, value);
        } catch (RuntimeException e) {
            throw new ConfigValueConversionException(type, path.toString(), value, e);
        }
    }
}
