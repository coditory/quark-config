package com.coditory.configio.api;

public class MissingConfigValueException extends ConfigioException {
    public static MissingConfigValueException missingConfigValueForPath(String path) {
        return new MissingConfigValueException("Missing config value for path: " + path);
    }

    public MissingConfigValueException(String message) {
        super(message);
    }
}
