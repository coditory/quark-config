package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.coditory.quark.config.Preconditions.expect;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;

public interface Config extends ConfigGetters {
    @NotNull
    static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    @NotNull
    static ConfigBuilder builder(@NotNull Config config) {
        expectNonNull(config, "config");
        return builder().putAll(config);
    }

    @NotNull
    static Config empty() {
        return ResolvableConfig.empty();
    }

    @NotNull
    static Config of(@NotNull String firstKey, @NotNull Object firstValue, Object... otherEntries) {
        expectNonBlank(firstKey);
        expectNonNull(otherEntries);
        expect(otherEntries.length % 2 == 0, "Expected even entries. Got: ", 2 + otherEntries.length);
        Map<String, Object> entries = new LinkedHashMap<>();
        entries.put(firstKey, firstValue);
        for (int i = 0; i < otherEntries.length / 2; i += 2) {
            expectNonNull(otherEntries[i], "config key");
            String key = Objects.toString(otherEntries[i]);
            entries.put(key, otherEntries[i + 1]);
        }
        return of(entries);
    }

    @NotNull
    static Config of(@NotNull Map<String, ?> entries) {
        expectNonNull(entries, "entries");
        return new ConfigBuilder(entries).build();
    }

    @NotNull
    Map<String, Object> toMap();

    @NotNull
    Map<String, Object> toFlatMap();

    boolean contains(@NotNull String path);

    @NotNull
    Config getSubConfig(@NotNull String path);

    @Nullable
    Config getSubConfigOrNull(@NotNull String path);

    @NotNull
    Config getSubConfigOrEmpty(@NotNull String path);

    @NotNull
    Config getSubConfig(@NotNull String path, @NotNull Config defaultValue);

    @NotNull
    Optional<Config> getSubConfigAsOptional(@NotNull String path);

    @NotNull
    String getPath();

    @NotNull
    MapConfigNode getRootNode();

    boolean isEmpty();

    @NotNull
    Config withHiddenSecrets();

    @NotNull
    default AuditableConfig auditable() {
        return AuditableConfig.of(this);
    }

    @NotNull
    default <T> T mapSubConfig(@NotNull String path, @NotNull Function<Config, T> configMapper) {
        Config subconfig = this.getSubConfig(path);
        return configMapper.apply(subconfig);
    }

    @Nullable
    default <T> T mapSubConfigOrNull(@NotNull String path, @NotNull Function<Config, T> configMapper) {
        Config subconfig = this.getSubConfigOrNull(path);
        if (subconfig == null) {
            return null;
        }
        return configMapper.apply(subconfig);
    }

    @NotNull
    default <T> T mapSubConfigOrEmpty(@NotNull String path, @NotNull Function<Config, T> configMapper) {
        Config subconfig = this.getSubConfigOrEmpty(path);
        return configMapper.apply(subconfig);
    }

    default <T> T mapAuditableSubConfigOrNull(@NotNull String path, @NotNull Function<AuditableConfig, T> configMapper) {
        Config subconfig = this.getSubConfigOrNull(path);
        if (subconfig == null) {
            return null;
        }
        return subconfig.mapAuditable(configMapper);
    }

    default <T> T mapAuditableSubConfigOrEmpty(@NotNull String path, @NotNull Function<AuditableConfig, T> configMapper) {
        Config subconfig = this.getSubConfigOrEmpty(path);
        return subconfig.mapAuditable(configMapper);
    }

    default <T> T mapAuditableSubConfig(@NotNull String path, @NotNull Function<AuditableConfig, T> configMapper) {
        return this.getSubConfig(path).mapAuditable(configMapper);
    }

    default <T> T mapAuditable(@NotNull Function<AuditableConfig, T> configMapper) {
        AuditableConfig auditableConfig = AuditableConfig.of(this);
        T result = configMapper.apply(auditableConfig);
        auditableConfig.failOnUnusedProperties();
        return result;
    }

    default void consumeAuditableSubConfig(@NotNull String path, @NotNull Consumer<AuditableConfig> configConsumer) {
        this.getSubConfig(path).consumeAuditable(configConsumer);
    }

    default void consumeAuditable(@NotNull Consumer<AuditableConfig> configConsumer) {
        AuditableConfig auditableConfig = AuditableConfig.of(this);
        configConsumer.accept(auditableConfig);
        auditableConfig.failOnUnusedProperties();
    }
}
