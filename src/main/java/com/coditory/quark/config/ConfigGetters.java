package com.coditory.quark.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;

import static com.coditory.quark.config.MissingConfigValueException.missingConfigValueForPath;

interface ConfigGetters {
    <T> Optional<T> getAsOptional(Class<T> type, String path);

    default <T> T get(Class<T> type, String path) {
        return getAsOptional(type, path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default <T> T getOrNull(Class<T> type, String path) {
        return get(type, path, null);
    }

    default <T> T get(Class<T> type, String path, T defaultValue) {
        return getAsOptional(type, path).orElse(defaultValue);
    }

    // GETTERS

    // String API

    default Optional<String> getStringAsOptional(String path) {
        return getAsOptional(String.class, path);
    }

    default String getString(String path) {
        return getStringAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default String getStringOrNull(String path) {
        return getString(path, null);
    }

    default String getString(String path, String defaultValue) {
        return getStringAsOptional(path).orElse(defaultValue);
    }

    // Object API

    default Optional<Object> getObjectAsOptional(String path) {
        return getAsOptional(Object.class, path);
    }

    default Object getObject(String path) {
        return getObjectAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Object getObjectOrNull(String path) {
        return getObject(path, null);
    }

    default Object getObject(String path, Object defaultValue) {
        return getObjectAsOptional(path).orElse(defaultValue);
    }

    // Boolean API

    default Optional<Boolean> getBooleanAsOptional(String path) {
        return getAsOptional(Boolean.class, path);
    }

    default Boolean getBoolean(String path) {
        return getBooleanAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Boolean getBooleanOrNull(String path) {
        return getBoolean(path, null);
    }

    default Boolean getBoolean(String path, Boolean defaultValue) {
        return getBooleanAsOptional(path).orElse(defaultValue);
    }

    // Short API

    default Optional<Short> getShortAsOptional(String path) {
        return getAsOptional(Short.class, path);
    }

    default Short getShort(String path) {
        return getShortAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Short getShortOrNull(String path) {
        return getShort(path, null);
    }

    default Short getShort(String path, Short defaultValue) {
        return getShortAsOptional(path).orElse(defaultValue);
    }

    // Byte API

    default Optional<Byte> getByteAsOptional(String path) {
        return getAsOptional(Byte.class, path);
    }

    default Byte getByte(String path) {
        return getByteAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Byte getByteOrNull(String path) {
        return getByte(path, null);
    }

    default Byte getByte(String path, Byte defaultValue) {
        return getByteAsOptional(path).orElse(defaultValue);
    }

    // Integer API

    default Optional<Integer> getIntegerAsOptional(String path) {
        return getAsOptional(Integer.class, path);
    }

    default Integer getInteger(String path) {
        return getIntegerAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Integer getIntegerOrNull(String path) {
        return getInteger(path, null);
    }

    default Integer getInteger(String path, Integer defaultValue) {
        return getIntegerAsOptional(path).orElse(defaultValue);
    }

    // Long API

    default Optional<Long> getLongAsOptional(String path) {
        return getAsOptional(Long.class, path);
    }

    default Long getLong(String path) {
        return getLongAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Long getLongOrNull(String path) {
        return getLong(path, null);
    }

    default Long getLong(String path, Long defaultValue) {
        return getLongAsOptional(path).orElse(defaultValue);
    }

    // Float API

    default Optional<Float> getFloatAsOptional(String path) {
        return getAsOptional(Float.class, path);
    }

    default Float getFloat(String path) {
        return getFloatAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Float getFloatOrNull(String path) {
        return getFloat(path, null);
    }

    default Float getFloat(String path, Float defaultValue) {
        return getFloatAsOptional(path).orElse(defaultValue);
    }

    // Double API

    default Optional<Double> getDoubleAsOptional(String path) {
        return getAsOptional(Double.class, path);
    }

    default Double getDouble(String path) {
        return getDoubleAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Double getDoubleOrNull(String path) {
        return getDouble(path, null);
    }

    default Double getDouble(String path, Double defaultValue) {
        return getDoubleAsOptional(path).orElse(defaultValue);
    }

    // BigDecimal API

    default Optional<BigDecimal> getBigDecimalAsOptional(String path) {
        return getAsOptional(BigDecimal.class, path);
    }

    default BigDecimal getBigDecimal(String path) {
        return getBigDecimalAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default BigDecimal getBigDecimalOrNull(String path) {
        return getBigDecimal(path, null);
    }

    default BigDecimal getBigDecimal(String path, BigDecimal defaultValue) {
        return getBigDecimalAsOptional(path).orElse(defaultValue);
    }

    // Instant API

    default Optional<Instant> getInstantAsOptional(String path) {
        return getAsOptional(Instant.class, path);
    }

    default Instant getInstant(String path) {
        return getInstantAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Instant getInstantOrNull(String path) {
        return getInstant(path, null);
    }

    default Instant getInstant(String path, Instant defaultValue) {
        return getInstantAsOptional(path).orElse(defaultValue);
    }

    // ZonedDateTime API

    default Optional<ZonedDateTime> getZonedDateTimeAsOptional(String path) {
        return getAsOptional(ZonedDateTime.class, path);
    }

    default ZonedDateTime getZonedDateTime(String path) {
        return getZonedDateTimeAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default ZonedDateTime getZonedDateTimeOrNull(String path) {
        return getZonedDateTime(path, null);
    }

    default ZonedDateTime getZonedDateTime(String path, ZonedDateTime defaultValue) {
        return getZonedDateTimeAsOptional(path).orElse(defaultValue);
    }

    // Duration API

    default Optional<Duration> getDurationAsOptional(String path) {
        return getAsOptional(Duration.class, path);
    }

    default Duration getDuration(String path) {
        return getDurationAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Duration getDurationOrNull(String path) {
        return getDuration(path, null);
    }

    default Duration getDuration(String path, Duration defaultValue) {
        return getDurationAsOptional(path).orElse(defaultValue);
    }

    // Locale API

    default Optional<Locale> getLocaleAsOptional(String path) {
        return getAsOptional(Locale.class, path);
    }

    default Locale getLocale(String path) {
        return getLocaleAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Locale getLocaleOrNull(String path) {
        return getLocale(path, null);
    }

    default Locale getLocale(String path, Locale defaultValue) {
        return getLocaleAsOptional(path).orElse(defaultValue);
    }

    // Currency API

    default Optional<Currency> getCurrencyAsOptional(String path) {
        return getAsOptional(Currency.class, path);
    }

    default Currency getCurrency(String path) {
        return getCurrencyAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default Currency getCurrencyOrNull(String path) {
        return getCurrency(path, null);
    }

    default Currency getCurrency(String path, Currency defaultValue) {
        return getCurrencyAsOptional(path).orElse(defaultValue);
    }

}

