package com.coditory.quark.config;

public class ConfigFormatter {
    public static String toJson(Config config) {
        return format(ConfigFormat.JSON, config.withHiddenSecrets());
    }

    public static String toJsonWithSecretsExposed(Config config) {
        return formatWithSecretsExposed(ConfigFormat.JSON, config);
    }

    public static String toYaml(Config config) {
        return format(ConfigFormat.YAML, config.withHiddenSecrets());
    }

    public static String toYamlWithSecretsExposed(Config config) {
        return formatWithSecretsExposed(ConfigFormat.YAML, config);
    }

    public static String toProperties(Config config) {
        return format(ConfigFormat.PROPERTIES, config.withHiddenSecrets());
    }

    public static String toPropertiesWithSecretsExposed(Config config) {
        return formatWithSecretsExposed(ConfigFormat.PROPERTIES, config);
    }

    private static String format(ConfigFormat format, Config config) {
        return format.stringify(config.withHiddenSecrets());
    }

    private static String formatWithSecretsExposed(ConfigFormat format, Config config) {
        return format.stringify(config);
    }
}
