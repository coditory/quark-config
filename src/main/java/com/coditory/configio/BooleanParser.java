package com.coditory.configio;

import com.coditory.configio.api.ConfigioParsingException;

class BooleanParser {
    static Boolean parseBoolean(String value) {
        String normalized = value.trim().toLowerCase();
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        throw new ConfigioParsingException("Could not parse boolean value: " + value);
    }
}
