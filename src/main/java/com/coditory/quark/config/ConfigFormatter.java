package com.coditory.quark.config;

import static com.coditory.quark.config.Preconditions.expectNonNull;

public class ConfigFormatter {
    public static String toJson(Config config) {
        expectNonNull(config, "config");
        return format(ConfigFormat.JSON, config.withHiddenSecrets());
    }

    public static String toJsonWithExposedSecrets(Config config) {
        expectNonNull(config, "config");
        return formatWithExposedSecrets(ConfigFormat.JSON, config);
    }

    public static String toYaml(Config config) {
        expectNonNull(config, "config");
        return format(ConfigFormat.YAML, config.withHiddenSecrets());
    }

    public static String toYamlWithExposedSecrets(Config config) {
        expectNonNull(config, "config");
        return formatWithExposedSecrets(ConfigFormat.YAML, config);
    }

    public static String toProperties(Config config) {
        expectNonNull(config, "config");
        return format(ConfigFormat.PROPERTIES, config.withHiddenSecrets());
    }

    public static String toPropertiesWithExposedSecrets(Config config) {
        expectNonNull(config, "config");
        return formatWithExposedSecrets(ConfigFormat.PROPERTIES, config);
    }

    private static String format(ConfigFormat format, Config config) {
        return format.stringify(config.withHiddenSecrets());
    }

    private static String formatWithExposedSecrets(ConfigFormat format, Config config) {
        return format.stringify(config);
    }
}
