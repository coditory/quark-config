package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.coditory.quark.config.MissingConfigValueException.missingConfigValueForPath;
import static com.coditory.quark.config.Preconditions.expectNonNull;

interface ConfigGetters {
    @NotNull
    <T> Optional<T> getAsOptional(@NotNull Class<T> type, @NotNull String path);

    @NotNull
    <T> Optional<List<T>> getListAsOptional(@NotNull Class<T> type, @NotNull String path);

    @NotNull
    default <T> T get(@NotNull Class<T> type, @NotNull String path) {
        expectNonNull(type, "type");
        expectNonNull(path, "path");
        return getAsOptional(type, path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default <T> T getOrNull(@NotNull Class<T> type, @NotNull String path) {
        expectNonNull(type, "type");
        expectNonNull(path, "path");
        return getAsOptional(type, path).orElse(null);
    }

    @NotNull
    default <T> T get(@NotNull Class<T> type, @NotNull String path, @NotNull T defaultValue) {
        expectNonNull(type, "type");
        expectNonNull(path, "path");
        expectNonNull(defaultValue, "defaultValue");
        return getAsOptional(type, path).orElse(defaultValue);
    }

    @NotNull
    default <T> List<T> getList(Class<T> type, String path) {
        expectNonNull(type, "type");
        expectNonNull(path, "path");
        return getListAsOptional(type, path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default <T> List<T> getListOrNull(@NotNull Class<T> type, @NotNull String path) {
        expectNonNull(type, "type");
        expectNonNull(path, "path");
        return getListAsOptional(type, path).orElse(null);
    }

    @NotNull
    default <T> List<T> getList(@NotNull Class<T> type, @NotNull String path, @NotNull List<T> defaultValue) {
        expectNonNull(type, "type");
        expectNonNull(path, "path");
        expectNonNull(defaultValue, "defaultValue");
        return getListAsOptional(type, path).orElse(defaultValue);
    }

    // GETTERS

    // String API

    @NotNull
    default Optional<String> getStringAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(String.class, path);
    }

    @NotNull
    default String getString(@NotNull String path) {
        expectNonNull(path, "path");
        return getStringAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default String getStringOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getStringAsOptional(path).orElse(null);
    }

    @NotNull
    default String getString(@NotNull String path, @NotNull String defaultValue) {
        expectNonNull(path, "path");
        expectNonNull(defaultValue, "defaultValue");
        return getStringAsOptional(path).orElse(defaultValue);
    }

    // Object API

    @NotNull
    default Optional<Object> getObjectAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Object.class, path);
    }

    @NotNull
    default Object getObject(@NotNull String path) {
        expectNonNull(path, "path");
        return getObjectAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Object getObjectOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getObjectAsOptional(path).orElse(null);
    }

    @NotNull
    default Object getObject(@NotNull String path, @NotNull Object defaultValue) {
        return getObjectAsOptional(path).orElse(defaultValue);
    }

    // Boolean API

    @NotNull
    default Optional<Boolean> getBooleanAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Boolean.class, path);
    }

    @NotNull
    default Boolean getBoolean(@NotNull String path) {
        expectNonNull(path, "path");
        return getBooleanAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Boolean getBooleanOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getBooleanAsOptional(path).orElse(null);
    }

    @NotNull
    default Boolean getBoolean(@NotNull String path, @NotNull Boolean defaultValue) {
        return getBooleanAsOptional(path).orElse(defaultValue);
    }

    // Short API

    @NotNull
    default Optional<Short> getShortAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Short.class, path);
    }

    @NotNull
    default Short getShort(@NotNull String path) {
        expectNonNull(path, "path");
        return getShortAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Short getShortOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getShortAsOptional(path).orElse(null);
    }

    @NotNull
    default Short getShort(@NotNull String path, @NotNull Short defaultValue) {
        return getShortAsOptional(path).orElse(defaultValue);
    }

    // Byte API

    @NotNull
    default Optional<Byte> getByteAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Byte.class, path);
    }

    @NotNull
    default Byte getByte(@NotNull String path) {
        expectNonNull(path, "path");
        return getByteAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Byte getByteOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getByteAsOptional(path).orElse(null);
    }

    @NotNull
    default Byte getByte(@NotNull String path, @NotNull Byte defaultValue) {
        return getByteAsOptional(path).orElse(defaultValue);
    }

    // Integer API

    @NotNull
    default Optional<Integer> getIntegerAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Integer.class, path);
    }

    @NotNull
    default Integer getInteger(@NotNull String path) {
        expectNonNull(path, "path");
        return getIntegerAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Integer getIntegerOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getIntegerAsOptional(path).orElse(null);
    }

    @NotNull
    default Integer getInteger(@NotNull String path, @NotNull Integer defaultValue) {
        return getIntegerAsOptional(path).orElse(defaultValue);
    }

    // Long API

    @NotNull
    default Optional<Long> getLongAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Long.class, path);
    }

    @NotNull
    default Long getLong(@NotNull String path) {
        expectNonNull(path, "path");
        return getLongAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Long getLongOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getLongAsOptional(path).orElse(null);
    }

    @NotNull
    default Long getLong(@NotNull String path, @NotNull Long defaultValue) {
        return getLongAsOptional(path).orElse(defaultValue);
    }

    // Float API

    @NotNull
    default Optional<Float> getFloatAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Float.class, path);
    }

    @NotNull
    default Float getFloat(@NotNull String path) {
        expectNonNull(path, "path");
        return getFloatAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Float getFloatOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getFloatAsOptional(path).orElse(null);
    }

    @NotNull
    default Float getFloat(@NotNull String path, @NotNull Float defaultValue) {
        return getFloatAsOptional(path).orElse(defaultValue);
    }

    // Double API

    @NotNull
    default Optional<Double> getDoubleAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Double.class, path);
    }

    @NotNull
    default Double getDouble(@NotNull String path) {
        expectNonNull(path, "path");
        return getDoubleAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Double getDoubleOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getDoubleAsOptional(path).orElse(null);
    }

    @NotNull
    default Double getDouble(@NotNull String path, @NotNull Double defaultValue) {
        return getDoubleAsOptional(path).orElse(defaultValue);
    }

    // BigDecimal API

    @NotNull
    default Optional<BigDecimal> getBigDecimalAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(BigDecimal.class, path);
    }

    @NotNull
    default BigDecimal getBigDecimal(@NotNull String path) {
        expectNonNull(path, "path");
        return getBigDecimalAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default BigDecimal getBigDecimalOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getBigDecimalAsOptional(path).orElse(null);
    }

    @NotNull
    default BigDecimal getBigDecimal(@NotNull String path, @NotNull BigDecimal defaultValue) {
        return getBigDecimalAsOptional(path).orElse(defaultValue);
    }

    // Instant API

    @NotNull
    default Optional<Instant> getInstantAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Instant.class, path);
    }

    @NotNull
    default Instant getInstant(@NotNull String path) {
        expectNonNull(path, "path");
        return getInstantAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Instant getInstantOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getInstantAsOptional(path).orElse(null);
    }

    @NotNull
    default Instant getInstant(@NotNull String path, @NotNull Instant defaultValue) {
        return getInstantAsOptional(path).orElse(defaultValue);
    }

    // ZonedDateTime API

    @NotNull
    default Optional<ZonedDateTime> getZonedDateTimeAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(ZonedDateTime.class, path);
    }

    @NotNull
    default ZonedDateTime getZonedDateTime(@NotNull String path) {
        expectNonNull(path, "path");
        return getZonedDateTimeAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default ZonedDateTime getZonedDateTimeOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getZonedDateTimeAsOptional(path).orElse(null);
    }

    @NotNull
    default ZonedDateTime getZonedDateTime(@NotNull String path, @NotNull ZonedDateTime defaultValue) {
        return getZonedDateTimeAsOptional(path).orElse(defaultValue);
    }

    // Duration API

    @NotNull
    default Optional<Duration> getDurationAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Duration.class, path);
    }

    @NotNull
    default Duration getDuration(@NotNull String path) {
        expectNonNull(path, "path");
        return getDurationAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Duration getDurationOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getDurationAsOptional(path).orElse(null);
    }

    @NotNull
    default Duration getDuration(@NotNull String path, @NotNull Duration defaultValue) {
        return getDurationAsOptional(path).orElse(defaultValue);
    }

    // Locale API

    @NotNull
    default Optional<Locale> getLocaleAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Locale.class, path);
    }

    @NotNull
    default Locale getLocale(@NotNull String path) {
        expectNonNull(path, "path");
        return getLocaleAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Locale getLocaleOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getLocaleAsOptional(path).orElse(null);
    }

    @NotNull
    default Locale getLocale(@NotNull String path, @NotNull Locale defaultValue) {
        return getLocaleAsOptional(path).orElse(defaultValue);
    }

    // Currency API

    @NotNull
    default Optional<Currency> getCurrencyAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getAsOptional(Currency.class, path);
    }

    @NotNull
    default Currency getCurrency(@NotNull String path) {
        expectNonNull(path, "path");
        return getCurrencyAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default Currency getCurrencyOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getCurrencyAsOptional(path).orElse(null);
    }

    @NotNull
    default Currency getCurrency(@NotNull String path, @NotNull Currency defaultValue) {
        return getCurrencyAsOptional(path).orElse(defaultValue);
    }

    // LIST GETTERS

    // String List API

    @NotNull
    default Optional<List<String>> getStringListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(String.class, path);
    }

    @NotNull
    default List<String> getStringList(@NotNull String path) {
        expectNonNull(path, "path");
        return getStringListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<String> getStringListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getStringListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<String> getStringListOrEmpty(@NotNull String path) {
        expectNonNull(path, "path");
        return getStringList(path, List.of());
    }

    @NotNull
    default List<String> getStringList(@NotNull String path, @NotNull List<String> defaultValue) {
        return getStringListAsOptional(path).orElse(defaultValue);
    }

    // Object List API

    @NotNull
    default Optional<List<Object>> getObjectListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Object.class, path);
    }

    @NotNull
    default List<Object> getObjectList(@NotNull String path) {
        expectNonNull(path, "path");
        return getObjectListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Object> getObjectListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getObjectListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Object> getObjectList(@NotNull String path, @NotNull List<Object> defaultValue) {
        return getObjectListAsOptional(path).orElse(defaultValue);
    }

    // Boolean List API

    @NotNull
    default Optional<List<Boolean>> getBooleanListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Boolean.class, path);
    }

    @NotNull
    default List<Boolean> getBooleanList(@NotNull String path) {
        expectNonNull(path, "path");
        return getBooleanListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Boolean> getBooleanListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getBooleanListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Boolean> getBooleanList(@NotNull String path, @NotNull List<Boolean> defaultValue) {
        return getBooleanListAsOptional(path).orElse(defaultValue);
    }

    // Short List API

    @NotNull
    default Optional<List<Short>> getShortListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Short.class, path);
    }

    @NotNull
    default List<Short> getShortList(@NotNull String path) {
        expectNonNull(path, "path");
        return getShortListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Short> getShortListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getShortListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Short> getShortList(@NotNull String path, @NotNull List<Short> defaultValue) {
        return getShortListAsOptional(path).orElse(defaultValue);
    }

    // Byte List API

    @NotNull
    default Optional<List<Byte>> getByteListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Byte.class, path);
    }

    @NotNull
    default List<Byte> getByteList(@NotNull String path) {
        expectNonNull(path, "path");
        return getByteListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Byte> getByteListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getByteListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Byte> getByteList(@NotNull String path, @NotNull List<Byte> defaultValue) {
        return getByteListAsOptional(path).orElse(defaultValue);
    }

    // Integer List API

    @NotNull
    default Optional<List<Integer>> getIntegerListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Integer.class, path);
    }

    @NotNull
    default List<Integer> getIntegerList(@NotNull String path) {
        expectNonNull(path, "path");
        return getIntegerListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Integer> getIntegerListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getIntegerListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Integer> getIntegerList(@NotNull String path, @NotNull List<Integer> defaultValue) {
        return getIntegerListAsOptional(path).orElse(defaultValue);
    }

    // Long List API

    @NotNull
    default Optional<List<Long>> getLongListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Long.class, path);
    }

    @NotNull
    default List<Long> getLongList(@NotNull String path) {
        expectNonNull(path, "path");
        return getLongListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Long> getLongListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getLongListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Long> getLongList(@NotNull String path, @NotNull List<Long> defaultValue) {
        return getLongListAsOptional(path).orElse(defaultValue);
    }

    // Float List API

    @NotNull
    default Optional<List<Float>> getFloatListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Float.class, path);
    }

    @NotNull
    default List<Float> getFloatList(@NotNull String path) {
        expectNonNull(path, "path");
        return getFloatListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Float> getFloatListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getFloatListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Float> getFloatList(@NotNull String path, @NotNull List<Float> defaultValue) {
        return getFloatListAsOptional(path).orElse(defaultValue);
    }

    // Double List API

    @NotNull
    default Optional<List<Double>> getDoubleListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Double.class, path);
    }

    @NotNull
    default List<Double> getDoubleList(@NotNull String path) {
        expectNonNull(path, "path");
        return getDoubleListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Double> getDoubleListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getDoubleListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Double> getDoubleList(@NotNull String path, @NotNull List<Double> defaultValue) {
        return getDoubleListAsOptional(path).orElse(defaultValue);
    }

    // BigDecimal List API

    @NotNull
    default Optional<List<BigDecimal>> getBigDecimalListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(BigDecimal.class, path);
    }

    @NotNull
    default List<BigDecimal> getBigDecimalList(@NotNull String path) {
        expectNonNull(path, "path");
        return getBigDecimalListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<BigDecimal> getBigDecimalListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getBigDecimalListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<BigDecimal> getBigDecimalList(@NotNull String path, @NotNull List<BigDecimal> defaultValue) {
        return getBigDecimalListAsOptional(path).orElse(defaultValue);
    }

    // Instant List API

    @NotNull
    default Optional<List<Instant>> getInstantListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Instant.class, path);
    }

    @NotNull
    default List<Instant> getInstantList(@NotNull String path) {
        expectNonNull(path, "path");
        return getInstantListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Instant> getInstantListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getInstantListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Instant> getInstantList(@NotNull String path, @NotNull List<Instant> defaultValue) {
        return getInstantListAsOptional(path).orElse(defaultValue);
    }

    // ZonedDateTime List API

    @NotNull
    default Optional<List<ZonedDateTime>> getZonedDateTimeListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(ZonedDateTime.class, path);
    }

    @NotNull
    default List<ZonedDateTime> getZonedDateTimeList(@NotNull String path) {
        expectNonNull(path, "path");
        return getZonedDateTimeListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<ZonedDateTime> getZonedDateTimeListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getZonedDateTimeListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<ZonedDateTime> getZonedDateTimeList(@NotNull String path, @NotNull List<ZonedDateTime> defaultValue) {
        return getZonedDateTimeListAsOptional(path).orElse(defaultValue);
    }

    // Duration List API

    @NotNull
    default Optional<List<Duration>> getDurationListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Duration.class, path);
    }

    @NotNull
    default List<Duration> getDurationList(@NotNull String path) {
        expectNonNull(path, "path");
        return getDurationListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Duration> getDurationListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getDurationListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Duration> getDurationList(@NotNull String path, @NotNull List<Duration> defaultValue) {
        return getDurationListAsOptional(path).orElse(defaultValue);
    }

    // Locale List API

    @NotNull
    default Optional<List<Locale>> getLocaleListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Locale.class, path);
    }

    @NotNull
    default List<Locale> getLocaleList(@NotNull String path) {
        expectNonNull(path, "path");
        return getLocaleListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Locale> getLocaleListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getLocaleListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Locale> getLocaleList(@NotNull String path, @NotNull List<Locale> defaultValue) {
        return getLocaleListAsOptional(path).orElse(defaultValue);
    }

    // Currency List API

    @NotNull
    default Optional<List<Currency>> getCurrencyListAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return getListAsOptional(Currency.class, path);
    }

    @NotNull
    default List<Currency> getCurrencyList(@NotNull String path) {
        expectNonNull(path, "path");
        return getCurrencyListAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Nullable
    default List<Currency> getCurrencyListOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getCurrencyListAsOptional(path).orElse(null);
    }

    @NotNull
    default List<Currency> getCurrencyList(@NotNull String path, @NotNull List<Currency> defaultValue) {
        return getCurrencyListAsOptional(path).orElse(defaultValue);
    }
}