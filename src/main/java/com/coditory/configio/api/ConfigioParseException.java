package com.coditory.configio.api;

class ConfigioParseException extends ConfigioException {
    public ConfigioParseException(String message) {
        super(message);
    }

    public ConfigioParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
