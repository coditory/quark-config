package com.coditory.config;

public class MissingConfigValueException extends ConfigException {
    public static MissingConfigValueException missingConfigValueForPath(String path) {
        return new MissingConfigValueException("Missing config value for path: " + path);
    }

    public MissingConfigValueException(String message) {
        super(message);
    }
}
