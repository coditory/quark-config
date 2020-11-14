package com.coditory.quark.config;

import java.util.Map;

import static com.coditory.quark.config.Expectations.expectNonNull;

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
