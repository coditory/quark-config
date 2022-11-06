package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.coditory.quark.config.ConfigValueParser.defaultConfigValueParser;
import static com.coditory.quark.config.MissingConfigValueException.missingConfigValueForPath;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;
import static com.coditory.quark.config.SecretHidingValueMapper.defaultSecretHidingValueMapper;
import static java.util.Collections.unmodifiableMap;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.entry;

final class ResolvableConfig implements Config {
    private final static ResolvableConfig EMPTY = new ResolvableConfig(
            MapConfigNode.emptyRoot(),
            defaultConfigValueParser(),
            defaultSecretHidingValueMapper()
    );

    public static ResolvableConfig empty() {
        return EMPTY;
    }

    private final ConfigValueParser valueParser;
    private final MapConfigNode root;
    private final ConfigEntryMapper secretHidingValueMapper;

    ResolvableConfig(
            MapConfigNode root,
            ConfigValueParser valueParser,
            ConfigEntryMapper secretHidingValueMapper
    ) {
        this.root = expectNonNull(root);
        this.valueParser = expectNonNull(valueParser);
        this.secretHidingValueMapper = expectNonNull(secretHidingValueMapper);
    }

    @NotNull
    @Override
    public Map<String, Object> toMap() {
        return root.unwrap();
    }

    @Override
    public boolean contains(@NotNull String path) {
        expectNonBlank(path, "path");
        return root.getOptionalNode(Path.parse(path))
                .isPresent();
    }

    @NotNull
    @Override
    public MapConfigNode getRootNode() {
        return root;
    }

    @NotNull
    @Override
    public Config withHiddenSecrets() {
        MapConfigNode mapped = root.mapLeaves(Path.root(), secretHidingValueMapper);
        return new ResolvableConfig(mapped, valueParser, secretHidingValueMapper);
    }

    private ResolvableConfig withRoot(MapConfigNode root) {
        return Objects.equals(this.root, root)
                ? this
                : new ResolvableConfig(root, valueParser, secretHidingValueMapper);
    }

    @NotNull
    @Override
    public Config getSubConfig(@NotNull String path) {
        expectNonBlank(path, "path");
        return getSubConfigAsOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @NotNull
    @Override
    public Config getSubConfigOrEmpty(@NotNull String path) {
        expectNonBlank(path, "path");
        return getSubConfig(path, withRoot(MapConfigNode.emptyRoot()));
    }

    @Nullable
    @Override
    public Config getSubConfigOrNull(@NotNull String path) {
        expectNonBlank(path, "path");
        return getSubConfigAsOptional(path)
                .orElse(null);
    }

    @NotNull
    @Override
    public Config getSubConfig(@NotNull String path, @NotNull Config defaultValue) {
        expectNonBlank(path, "path");
        return getSubConfigAsOptional(path)
                .orElse(defaultValue);
    }

    @NotNull
    @Override
    public Optional<Config> getSubConfigAsOptional(@NotNull String path) {
        expectNonBlank(path, "path");
        return root.getOptionalNode(Path.parse(path))
                .filter(node -> node instanceof MapConfigNode)
                .map(node -> withRoot((MapConfigNode) node));
    }

    @NotNull
    @Override
    public <T> Optional<List<T>> getListAsOptional(@NotNull Class<T> type, @NotNull String path) {
        return getOptional(path)
                .map(value -> value.getAsList(valueParser, type));
    }

    @NotNull
    @Override
    public <T> Optional<T> getAsOptional(@NotNull Class<T> type, @NotNull String path) {
        return getOptional(path)
                .map(value -> value.getAs(valueParser, type));
    }

    private Optional<ConfigValue> getOptional(String path) {
        expectNonBlank(path, "path");
        return getOptional(Path.parse(path));
    }

    private Optional<ConfigValue> getOptional(Path path) {
        expectNonNull(path, "path");
        return root.getOptional(path)
                .map(value -> new ConfigValue(path, value));
    }

    @Override
    public boolean isEmpty() {
        return root.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolvableConfig config = (ResolvableConfig) o;
        return valueParser.equals(config.valueParser) &&
                root.equals(config.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueParser, root);
    }

    @Override
    public String toString() {
        return withHiddenSecrets()
                .toMap()
                .toString();
    }

    @NotNull
    public Map<String, Object> toFlatMap() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        root.entries().stream()
                .map(entry -> entry(entry.getKey().toString(), entry.getValue()))
                .sorted(comparingByKey())
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return unmodifiableMap(result);
    }
}
