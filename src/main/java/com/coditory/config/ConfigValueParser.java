package com.coditory.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

class ConfigValueParser {
    static final List<ValueParser> DEFAULT_VALUE_PARSERS = List.of(
            ValueParser.forType(Boolean.class, BooleanParser::parseBoolean),
            ValueParser.forType(Short.class, Short::parseShort),
            ValueParser.forType(Byte.class, Byte::parseByte),
            ValueParser.forType(Integer.class, Integer::parseInt),
            ValueParser.forType(Long.class, Long::parseLong),
            ValueParser.forType(Float.class, Float::parseFloat),
            ValueParser.forType(Double.class, Double::parseDouble),
            ValueParser.forType(BigDecimal.class, BigDecimal::new),
            ValueParser.forType(ZonedDateTime.class, value -> ZonedDateTime.parse(value, ISO_OFFSET_DATE_TIME)),
            ValueParser.forType(Instant.class, Instant::parse),
            ValueParser.forType(Duration.class, DurationParser::parseDuration),
            ValueParser.forType(Locale.class, LocaleParser::parseLocale),
            ValueParser.forType(Currency.class, Currency::getInstance)
    );

    private static final ConfigValueParser DEFAULT_VALUE_PARSER = new ConfigValueParser(DEFAULT_VALUE_PARSERS);

    static ConfigValueParser defaultConfigValueParser() {
        return DEFAULT_VALUE_PARSER;
    }

    private final List<ValueParser> valueParsers;

    ConfigValueParser(List<ValueParser> valueParsers) {
        this.valueParsers = List.copyOf(valueParsers);
    }

    ConfigValueParser addParser(ValueParser parser) {
        List<ValueParser> newParsers = new ArrayList<>(valueParsers);
        newParsers.add(parser);
        return Objects.equals(newParsers, valueParsers)
                ? this
                : new ConfigValueParser(newParsers);
    }

    <T> T parse(Class<T> type, String value) {
        return valueParsers.stream()
                .filter(p -> p.isApplicable(type, value))
                .findFirst()
                .orElseThrow(() -> new ConfigParseException("No parser for type: " + type))
                .parse(type, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigValueParser that = (ConfigValueParser) o;
        return Objects.equals(valueParsers, that.valueParsers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueParsers);
    }
}
