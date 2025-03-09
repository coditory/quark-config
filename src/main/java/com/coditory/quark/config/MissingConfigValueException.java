package com.coditory.quark.config;

public class MissingConfigValueException extends ConfigException {
    public static MissingConfigValueException missingConfigValueForPath(String path) {
        return new MissingConfigValueException("Missing config value for path: " + path);
    }

    public static MissingConfigValueException missingConfigValueForPath(String parentPath, String path) {
        String joined = Path.parse(parentPath).add(path).toString();
        return missingConfigValueForPath(joined);
    }

    public MissingConfigValueException(String message) {
        super(message);
    }
}
