package com.coditory.quark.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.coditory.quark.config.MissingConfigValueException.missingConfigValueForPath;

interface ConfigGetters {
    <T> Optional<T> getAsOptional(Class<T> type, String path);

    <T> Optional<List<T>> getListAsOptional(Class<T> type, String path);

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

    default <T> List<T> getList(Class<T> type, String path) {
        return getListAsOptional(type, path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default <T> List<T> getListOrNull(Class<T> type, String path) {
        return getList(type, path, null);
    }

    default <T> List<T> getList(Class<T> type, String path, List<T> defaultValue) {
        return getListAsOptional(type, path).orElse(defaultValue);
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

    // LIST GETTERS

    // String List API

    default Optional<List<String>> getStringListAsOptional(String path) {
        return getListAsOptional(String.class, path);
    }

    default List<String> getStringList(String path) {
        return getStringListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<String> getStringListOrNull(String path) {
        return getStringList(path, null);
    }

    default List<String> getStringListOrEmpty(String path) {
        return getStringList(path, List.of());
    }

    default List<String> getStringList(String path, List<String> defaultValue) {
        return getStringListAsOptional(path).orElse(defaultValue);
    }

    // Object List API

    default Optional<List<Object>> getObjectListAsOptional(String path) {
        return getListAsOptional(Object.class, path);
    }

    default List<Object> getObjectList(String path) {
        return getObjectListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Object> getObjectListOrNull(String path) {
        return getObjectList(path, null);
    }

    default List<Object> getObjectList(String path, List<Object> defaultValue) {
        return getObjectListAsOptional(path).orElse(defaultValue);
    }

    // Boolean List API

    default Optional<List<Boolean>> getBooleanListAsOptional(String path) {
        return getListAsOptional(Boolean.class, path);
    }

    default List<Boolean> getBooleanList(String path) {
        return getBooleanListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Boolean> getBooleanListOrNull(String path) {
        return getBooleanList(path, null);
    }

    default List<Boolean> getBooleanList(String path, List<Boolean> defaultValue) {
        return getBooleanListAsOptional(path).orElse(defaultValue);
    }

    // Short List API

    default Optional<List<Short>> getShortListAsOptional(String path) {
        return getListAsOptional(Short.class, path);
    }

    default List<Short> getShortList(String path) {
        return getShortListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Short> getShortListOrNull(String path) {
        return getShortList(path, null);
    }

    default List<Short> getShortList(String path, List<Short> defaultValue) {
        return getShortListAsOptional(path).orElse(defaultValue);
    }

    // Byte List API

    default Optional<List<Byte>> getByteListAsOptional(String path) {
        return getListAsOptional(Byte.class, path);
    }

    default List<Byte> getByteList(String path) {
        return getByteListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Byte> getByteListOrNull(String path) {
        return getByteList(path, null);
    }

    default List<Byte> getByteList(String path, List<Byte> defaultValue) {
        return getByteListAsOptional(path).orElse(defaultValue);
    }

    // Integer List API

    default Optional<List<Integer>> getIntegerListAsOptional(String path) {
        return getListAsOptional(Integer.class, path);
    }

    default List<Integer> getIntegerList(String path) {
        return getIntegerListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Integer> getIntegerListOrNull(String path) {
        return getIntegerList(path, null);
    }

    default List<Integer> getIntegerList(String path, List<Integer> defaultValue) {
        return getIntegerListAsOptional(path).orElse(defaultValue);
    }

    // Long List API

    default Optional<List<Long>> getLongListAsOptional(String path) {
        return getListAsOptional(Long.class, path);
    }

    default List<Long> getLongList(String path) {
        return getLongListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Long> getLongListOrNull(String path) {
        return getLongList(path, null);
    }

    default List<Long> getLongList(String path, List<Long> defaultValue) {
        return getLongListAsOptional(path).orElse(defaultValue);
    }

    // Float List API

    default Optional<List<Float>> getFloatListAsOptional(String path) {
        return getListAsOptional(Float.class, path);
    }

    default List<Float> getFloatList(String path) {
        return getFloatListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Float> getFloatListOrNull(String path) {
        return getFloatList(path, null);
    }

    default List<Float> getFloatList(String path, List<Float> defaultValue) {
        return getFloatListAsOptional(path).orElse(defaultValue);
    }

    // Double List API

    default Optional<List<Double>> getDoubleListAsOptional(String path) {
        return getListAsOptional(Double.class, path);
    }

    default List<Double> getDoubleList(String path) {
        return getDoubleListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Double> getDoubleListOrNull(String path) {
        return getDoubleList(path, null);
    }

    default List<Double> getDoubleList(String path, List<Double> defaultValue) {
        return getDoubleListAsOptional(path).orElse(defaultValue);
    }

    // BigDecimal List API

    default Optional<List<BigDecimal>> getBigDecimalListAsOptional(String path) {
        return getListAsOptional(BigDecimal.class, path);
    }

    default List<BigDecimal> getBigDecimalList(String path) {
        return getBigDecimalListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<BigDecimal> getBigDecimalListOrNull(String path) {
        return getBigDecimalList(path, null);
    }

    default List<BigDecimal> getBigDecimalList(String path, List<BigDecimal> defaultValue) {
        return getBigDecimalListAsOptional(path).orElse(defaultValue);
    }

    // Instant List API

    default Optional<List<Instant>> getInstantListAsOptional(String path) {
        return getListAsOptional(Instant.class, path);
    }

    default List<Instant> getInstantList(String path) {
        return getInstantListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Instant> getInstantListOrNull(String path) {
        return getInstantList(path, null);
    }

    default List<Instant> getInstantList(String path, List<Instant> defaultValue) {
        return getInstantListAsOptional(path).orElse(defaultValue);
    }

    // ZonedDateTime List API

    default Optional<List<ZonedDateTime>> getZonedDateTimeListAsOptional(String path) {
        return getListAsOptional(ZonedDateTime.class, path);
    }

    default List<ZonedDateTime> getZonedDateTimeList(String path) {
        return getZonedDateTimeListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<ZonedDateTime> getZonedDateTimeListOrNull(String path) {
        return getZonedDateTimeList(path, null);
    }

    default List<ZonedDateTime> getZonedDateTimeList(String path, List<ZonedDateTime> defaultValue) {
        return getZonedDateTimeListAsOptional(path).orElse(defaultValue);
    }

    // Duration List API

    default Optional<List<Duration>> getDurationListAsOptional(String path) {
        return getListAsOptional(Duration.class, path);
    }

    default List<Duration> getDurationList(String path) {
        return getDurationListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Duration> getDurationListOrNull(String path) {
        return getDurationList(path, null);
    }

    default List<Duration> getDurationList(String path, List<Duration> defaultValue) {
        return getDurationListAsOptional(path).orElse(defaultValue);
    }

    // Locale List API

    default Optional<List<Locale>> getLocaleListAsOptional(String path) {
        return getListAsOptional(Locale.class, path);
    }

    default List<Locale> getLocaleList(String path) {
        return getLocaleListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Locale> getLocaleListOrNull(String path) {
        return getLocaleList(path, null);
    }

    default List<Locale> getLocaleList(String path, List<Locale> defaultValue) {
        return getLocaleListAsOptional(path).orElse(defaultValue);
    }

    // Currency List API

    default Optional<List<Currency>> getCurrencyListAsOptional(String path) {
        return getListAsOptional(Currency.class, path);
    }

    default List<Currency> getCurrencyList(String path) {
        return getCurrencyListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    default List<Currency> getCurrencyListOrNull(String path) {
        return getCurrencyList(path, null);
    }

    default List<Currency> getCurrencyList(String path, List<Currency> defaultValue) {
        return getCurrencyListAsOptional(path).orElse(defaultValue);
    }
}

