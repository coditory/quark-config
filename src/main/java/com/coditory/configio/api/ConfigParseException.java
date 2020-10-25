package com.coditory.configio.api;

public class ConfigParseException extends ConfigException {
    public ConfigParseException(String message) {
        super(message);
    }

    public ConfigParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
