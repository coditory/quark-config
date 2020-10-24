package com.coditory.configio.api;

public class ConfigioParsingException extends ConfigioException {
    public ConfigioParsingException(String message) {
        super(message);
    }

    public ConfigioParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
