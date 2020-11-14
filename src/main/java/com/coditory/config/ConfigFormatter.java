package com.coditory.config;

import static com.coditory.config.ConfigFormat.JSON;
import static com.coditory.config.ConfigFormat.PROPERTIES;
import static com.coditory.config.ConfigFormat.YAML;

public class ConfigFormatter {
    public static String toJson(Config config) {
        return format(JSON, config.withHiddenSecrets());
    }

    public static String toJsonWithSecretsExposed(Config config) {
        return formatWithSecretsExposed(JSON, config);
    }

    public static String toYaml(Config config) {
        return format(YAML, config.withHiddenSecrets());
    }

    public static String toYamlWithSecretsExposed(Config config) {
        return formatWithSecretsExposed(YAML, config);
    }

    public static String toProperties(Config config) {
        return format(PROPERTIES, config.withHiddenSecrets());
    }

    public static String toPropertiesWithSecretsExposed(Config config) {
        return formatWithSecretsExposed(PROPERTIES, config);
    }

    private static String format(ConfigFormat format, Config config) {
        return format.stringify(config.withHiddenSecrets());
    }

    private static String formatWithSecretsExposed(ConfigFormat format, Config config) {
        return format.stringify(config);
    }
}
