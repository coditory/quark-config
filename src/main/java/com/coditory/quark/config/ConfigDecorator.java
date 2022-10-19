package com.coditory.quark.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coditory.quark.config.Preconditions.expectNonNull;

abstract class ConfigDecorator implements Config {
    private final Config config;

    public ConfigDecorator(Config config) {
        this.config = expectNonNull(config);
    }

    @Override
    public Map<String, Object> toMap() {
        return config.toMap();
    }

    @Override
    public Map<String, Object> toFlatMap() {
        return config.toFlatMap();
    }

    @Override
    public boolean contains(String path) {
        return config.contains(path);
    }

    @Override
    public Config withDefault(String path, Object value) {
        return config.withDefault(path, value);
    }

    @Override
    public Config withDefaults(Config config) {
        return config.withDefaults(config);
    }

    @Override
    public Config withValue(String path, Object value) {
        return config.withValue(path, value);
    }

    @Override
    public Config withValues(Config config) {
        return config.withValues(config);
    }

    @Override
    public Config filterValues(ConfigEntryPredicate predicate, ConfigRemoveOptions options) {
        return config.filterValues(predicate, options);
    }

    @Override
    public Config mapValues(ConfigEntryMapper mapper) {
        return config.mapValues(mapper);
    }

    @Override
    public Config removeEmptyProperties() {
        return config.removeEmptyProperties();
    }

    @Override
    public Config remove(String path, ConfigRemoveOptions options) {
        return config.remove(path, options);
    }

    @Override
    public Config getSubConfig(String path) {
        return config.getSubConfig(path);
    }

    @Override
    public Config getSubConfigOrEmpty(String path) {
        return config.getSubConfigOrEmpty(path);
    }

    @Override
    public Config getSubConfig(String path, Config defaultValue) {
        return config.getSubConfig(path, defaultValue);
    }

    @Override
    public Optional<Config> getSubConfigOptional(String path) {
        return config.getSubConfigOptional(path);
    }

    @Override
    public ConfigNode getRootNode() {
        return config.getRootNode();
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }

    @Override
    public Config withHiddenSecrets() {
        return config.withHiddenSecrets();
    }

    @Override
    public <T> Optional<T> getAsOptional(Class<T> type, String path) {
        return config.getAsOptional(type, path);
    }

    @Override
    public <T> Optional<List<T>> getListAsOptional(Class<T> type, String path) {
        return config.getListAsOptional(type, path);
    }
}
