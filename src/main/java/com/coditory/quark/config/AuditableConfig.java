package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParents;

public final class AuditableConfig extends ConfigDecorator {
    private static final Object USED_MARKER = new Object();
    private Config unreadConfig;

    static AuditableConfig of(Config config) {
        if (config instanceof AuditableConfig) {
            return (AuditableConfig) config;
        }
        return new AuditableConfig(config);
    }

    AuditableConfig(Config config) {
        super(config);
        this.unreadConfig = config;
    }

    @NotNull
    @Override
    public Config getSubConfig(@NotNull String path) {
        markAsRead(path);
        return super.getSubConfig(path);
    }

    @NotNull
    @Override
    public Config getSubConfigOrEmpty(@NotNull String path) {
        markAsRead(path);
        return super.getSubConfigOrEmpty(path);
    }

    @NotNull
    @Override
    public Config getSubConfig(@NotNull String path, @NotNull Config defaultValue) {
        markAsRead(path);
        return super.getSubConfig(path, defaultValue);
    }

    @NotNull
    @Override
    public Optional<Config> getSubConfigAsOptional(@NotNull String path) {
        markAsRead(path);
        return super.getSubConfigAsOptional(path);
    }

    @NotNull
    @Override
    public <T> Optional<List<T>> getListAsOptional(@NotNull Class<T> type, @NotNull String path) {
        markAsRead(path);
        return super.getListAsOptional(type, path);
    }

    @NotNull
    @Override
    public <T> Optional<T> getAsOptional(@NotNull Class<T> type, @NotNull String path) {
        markAsRead(path);
        return super.getAsOptional(type, path);
    }

    @NotNull
    public AuditableConfig markAsRead(@NotNull String path) {
        if (unreadConfig.contains(path)) {
            unreadConfig = Config.builder(unreadConfig)
                    .put(path, USED_MARKER)
                    .build();
        }
        return this;
    }

    @NotNull
    public Config getUnusedProperties() {
        return Config.builder()
                .putAll(unreadConfig)
                .filterValues(
                        (path, value) -> !Objects.equals(value, USED_MARKER),
                        removeEmptyParents()
                ).build();
    }

    public void throwErrorOnUnusedProperties() {
        Map<String, Object> properties = unreadConfig.toFlatMap().entrySet().stream()
                .filter(entry -> entry.getValue() != USED_MARKER)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        if (!properties.isEmpty()) {
            int limit = 5;
            List<String> names = properties.keySet().stream().sorted().toList();
            String limitedNames = names.stream().limit(5).collect(Collectors.joining("\n"));
            if (names.size() > limit) {
                limitedNames = limitedNames + "\n...";
            }
            throw new ConfigUnusedPropertiesException("Detected unused config properties:\n" + limitedNames);
        }
    }
}
