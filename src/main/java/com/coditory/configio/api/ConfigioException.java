package com.coditory.configio.api;

public class ConfigioException extends RuntimeException {
    public ConfigioException(String message) {
        super(message);
    }

    public ConfigioException(String message, Throwable cause) {
        super(message, cause);
    }
}
