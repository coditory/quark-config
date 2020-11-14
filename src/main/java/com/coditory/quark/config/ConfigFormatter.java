package com.coditory.quark.config;

public class ConfigFormatter {
    public static String toJson(Config config) {
        return format(ConfigFormat.JSON, config.withHiddenSecrets());
    }

    public static String toJsonWithExposedSecrets(Config config) {
        return formatWithExposedSecrets(ConfigFormat.JSON, config);
    }

    public static String toYaml(Config config) {
        return format(ConfigFormat.YAML, config.withHiddenSecrets());
    }

    public static String toYamlWithExposedSecrets(Config config) {
        return formatWithExposedSecrets(ConfigFormat.YAML, config);
    }

    public static String toProperties(Config config) {
        return format(ConfigFormat.PROPERTIES, config.withHiddenSecrets());
    }

    public static String toPropertiesWithExposedSecrets(Config config) {
        return formatWithExposedSecrets(ConfigFormat.PROPERTIES, config);
    }

    private static String format(ConfigFormat format, Config config) {
        return format.stringify(config.withHiddenSecrets());
    }

    private static String formatWithExposedSecrets(ConfigFormat format, Config config) {
        return format.stringify(config);
    }
}
