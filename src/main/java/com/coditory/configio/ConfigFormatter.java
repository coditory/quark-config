package com.coditory.configio;

import static com.coditory.configio.ConfigFormat.*;

public class ConfigFormatter {
    public static String toJson(Config config) {
        return JSON.stringify(config);
    }

    public static String toYaml(Config config) {
        return YAML.stringify(config);
    }

    public static String toProperties(Config config) {
        return PROPERTIES.stringify(config);
    }
}
