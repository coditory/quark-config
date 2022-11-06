package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coditory.quark.config.Preconditions.expectNonNull;

abstract class ConfigDecorator implements Config {
    private final Config config;

    public ConfigDecorator(Config config) {
        this.config = expectNonNull(config);
    }

    @NotNull
    @Override
    public Map<String, Object> toMap() {
        return config.toMap();
    }

    @NotNull
    @Override
    public Map<String, Object> toFlatMap() {
        return config.toFlatMap();
    }

    @Override
    public boolean contains(@NotNull String path) {
        expectNonNull(path, "path");
        return config.contains(path);
    }

    @NotNull
    @Override
    public Config getSubConfig(@NotNull String path) {
        expectNonNull(path, "path");
        return config.getSubConfig(path);
    }

    @NotNull
    @Override
    public Config getSubConfigOrEmpty(@NotNull String path) {
        expectNonNull(path, "path");
        return config.getSubConfigOrEmpty(path);
    }

    @Nullable
    @Override
    public Config getSubConfigOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return config.getSubConfigOrNull(path);
    }

    @NotNull
    @Override
    public Config getSubConfig(@NotNull String path, @NotNull Config defaultValue) {
        expectNonNull(path, "path");
        expectNonNull(defaultValue, "defaultValue");
        return config.getSubConfig(path, defaultValue);
    }

    @NotNull
    @Override
    public Optional<Config> getSubConfigAsOptional(@NotNull String path) {
        expectNonNull(path, "path");
        return config.getSubConfigAsOptional(path);
    }

    @NotNull
    @Override
    public MapConfigNode getRootNode() {
        return config.getRootNode();
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }

    @NotNull
    @Override
    public Config withHiddenSecrets() {
        return config.withHiddenSecrets();
    }

    @NotNull
    @Override
    public <T> Optional<T> getAsOptional(@NotNull Class<T> type, @NotNull String path) {
        expectNonNull(type, "type");
        expectNonNull(path, "path");
        return config.getAsOptional(type, path);
    }

    @NotNull
    @Override
    public <T> Optional<List<T>> getListAsOptional(@NotNull Class<T> type, @NotNull String path) {
        expectNonNull(type, "type");
        expectNonNull(path, "path");
        return config.getListAsOptional(type, path);
    }
}
