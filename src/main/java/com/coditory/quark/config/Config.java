package com.coditory.quark.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.coditory.quark.config.Preconditions.expect;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;

public interface Config extends ConfigGetters {
    static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    static ConfigBuilder builder(Config config) {
        return builder().putAll(config);
    }

    static Config empty() {
        return ResolvableConfig.empty();
    }

    static Config of(String firstKey, Object firstValue, Object... otherEntries) {
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

    static Config of(Map<String, ?> entries) {
        expectNonNull(entries, "entries");
        return new ConfigBuilder(entries).build();
    }

    Map<String, Object> toMap();

    Map<String, Object> toFlatMap();

    boolean contains(String path);

    Config getSubConfig(String path);

    Config getSubConfigOrEmpty(String path);

    Config getSubConfig(String path, Config defaultValue);

    Optional<Config> getSubConfigOptional(String path);

    MapConfigNode getRootNode();

    boolean isEmpty();

    Config withHiddenSecrets();

    default AuditableConfig auditable() {
        return AuditableConfig.of(this);
    }

}
