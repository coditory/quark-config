package com.coditory.quark.config;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DurationParser {
    private static final Pattern pattern = Pattern.compile("\\d+(\\.\\d+)? *(ms|s|m|h|d)");

    static Duration parseDuration(String value) {
        if (value.startsWith("-P") || value.startsWith("P")) {
            return durationParse(value);
        }
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            String unit = matcher.group(2).toUpperCase();
            String number = value.substring(0, value.length() - unit.length()).trim();
            float durationNumber = Float.parseFloat(number);
            if (unit.equals("MS")) {
                unit = "S";
                durationNumber = durationNumber / 1000;
            } else if (unit.equals("D")) {
                unit = "H";
                durationNumber = durationNumber * 24;
            }
            return durationNumber % 1 == 0
                    ? durationParse("PT" + (int) durationNumber + unit)
                    : durationParse("PT" + durationNumber + unit);
        }
        throw new ConfigParseException("Could not parse Duration value: " + value);
    }

    private static Duration durationParse(String value) {
        try {
            return Duration.parse(value);
        } catch (DateTimeParseException e) {
            throw new ConfigParseException("Could not parse Duration value: " + value);
        }
    }
}
