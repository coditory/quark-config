package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

import static com.coditory.quark.config.Preconditions.expectNonNull;

public class ConfigFormatter {
    @NotNull
    public static String toJson(@NotNull Config config) {
        expectNonNull(config, "config");
        return format(ConfigFormat.JSON, config.withHiddenSecrets());
    }

    @NotNull
    public static String toJsonWithExposedSecrets(@NotNull Config config) {
        expectNonNull(config, "config");
        return formatWithExposedSecrets(ConfigFormat.JSON, config);
    }

    @NotNull
    public static String toYaml(@NotNull Config config) {
        expectNonNull(config, "config");
        return format(ConfigFormat.YAML, config.withHiddenSecrets());
    }

    @NotNull
    public static String toYamlWithExposedSecrets(@NotNull Config config) {
        expectNonNull(config, "config");
        return formatWithExposedSecrets(ConfigFormat.YAML, config);
    }

    @NotNull
    public static String toProperties(@NotNull Config config) {
        expectNonNull(config, "config");
        return format(ConfigFormat.PROPERTIES, config.withHiddenSecrets());
    }

    @NotNull
    public static String toPropertiesWithExposedSecrets(@NotNull Config config) {
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
