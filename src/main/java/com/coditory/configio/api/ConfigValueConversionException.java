package com.coditory.configio.api;

public class ConfigValueConversionException extends ConfigException {
    public ConfigValueConversionException(Class<?> type, String path, Object value) {
        this(type, path, value, null);
    }

    public ConfigValueConversionException(Class<?> type, String path, Object value, Exception e) {
        this(String.format(
                "Could not convert value to %s. Path %s, Value: %s",
                type.getSimpleName(), path, value
        ), e);
    }

    public ConfigValueConversionException(String message) {
        super(message, null);
    }

    public ConfigValueConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
