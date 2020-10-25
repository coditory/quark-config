package com.coditory.configio;

import com.coditory.configio.api.ConfigParseException;

class BooleanParser {
    static Boolean parseBoolean(String value) {
        String normalized = value.trim().toLowerCase();
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        throw new ConfigParseException("Could not parse boolean value: " + value);
    }
}
