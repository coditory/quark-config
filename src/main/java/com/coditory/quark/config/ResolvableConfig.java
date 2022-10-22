package com.coditory.quark.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParents;
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

    @Override
    public Map<String, Object> toMap() {
        return root.unwrap();
    }

    @Override
    public boolean contains(String path) {
        expectNonBlank(path, "path");
        return root.getOptionalNode(Path.parse(path))
                .isPresent();
    }

    @Override
    public ResolvableConfig withDefault(String path, Object value) {
        expectNonBlank(path, "path");
        expectNonNull(value, "value");
        Path parsed = Path.parse(path);
        MapConfigNode newRoot = root.addIfMissing(Path.root(), parsed, value);
        return withRoot(newRoot);
    }

    @Override
    public ResolvableConfig withDefaults(Config config) {
        expectNonNull(config, "config");
        MapConfigNode mergedRoot = root.withDefaults(config.getRootNode());
        return withRoot(mergedRoot);
    }

    @Override
    public ResolvableConfig withValue(String path, Object value) {
        expectNonBlank(path, "path");
        expectNonNull(value, "value");
        Path parsed = Path.parse(path);
        ConfigNode newRoot = root.addOrReplace(Path.root(), parsed, value);
        if (!(newRoot instanceof MapConfigNode)) {
            throw new InvalidConfigPathException("Expected root node to be a map. Got: " + newRoot.getClass().getSimpleName());
        }
        return withRoot((MapConfigNode) newRoot);
    }

    @Override
    public ResolvableConfig withValues(Config config) {
        expectNonNull(config, "config");
        MapConfigNode mergedRoot = config.getRootNode().withDefaults(this.root);
        return withRoot(mergedRoot);
    }

    @Override
    public MapConfigNode getRootNode() {
        return root;
    }

    public ResolvableConfig resolveExpressions(Config values) {
        expectNonNull(values, "values");
        return resolveExpressions(values, Expression::failOnUnresolved);
    }

    public ResolvableConfig resolveExpressionsOrSkip(Config values) {
        expectNonNull(values, "values");
        return resolveExpressions(values, Expression::unwrap);
    }

    private ResolvableConfig resolveExpressions(Config variables, Function<Object, Object> leafMapper) {
        ResolvableConfig configWithExpressions = this.mapValues(ExpressionParser::parse);
        ResolvableConfig configWithExpressionsAndVariables = configWithExpressions
                .withValues(variables)
                .mapValues(ExpressionParser::parse);
        ExpressionResolver resolver = new ExpressionResolver(configWithExpressionsAndVariables);
        return this.mapValues(ExpressionParser::parse)
                .mapValues(resolver::resolve)
                .mapValues(leafMapper);
    }

    @Override
    public ResolvableConfig withHiddenSecrets() {
        return mapValues(secretHidingValueMapper);
    }

    private ResolvableConfig withRoot(MapConfigNode root) {
        return Objects.equals(this.root, root)
                ? this
                : new ResolvableConfig(root, valueParser, secretHidingValueMapper);
    }

    @Override
    public ResolvableConfig removeEmptyProperties() {
        ConfigEntryPredicate predicate = (path, value) -> {
            if (value instanceof List<?> && ((List<?>) value).isEmpty()) {
                return false;
            } else if (value instanceof Map<?, ?> && ((Map<?, ?>) value).isEmpty()) {
                return false;
            }
            return true;
        };
        MapConfigNode mapped = root.filterLeaves(Path.root(), predicate, removeEmptyParents());
        return mapped == null ? empty() : withRoot(mapped);
    }

    @Override
    public ResolvableConfig filterValues(ConfigEntryPredicate predicate, ConfigRemoveOptions options) {
        MapConfigNode mapped = root.filterLeaves(Path.root(), predicate, options);
        return mapped == null ? empty() : withRoot(mapped);
    }

    @Override
    public ResolvableConfig mapValues(Function<Object, Object> mapper) {
        return mapValues((path, value) -> mapper.apply(value));
    }

    @Override
    public ResolvableConfig mapValues(ConfigEntryMapper mapper) {
        MapConfigNode mapped = root.mapLeaves(Path.root(), mapper);
        return withRoot(mapped);
    }

    @Override
    public ResolvableConfig remove(String path, ConfigRemoveOptions options) {
        expectNonBlank(path, "path");
        Path parsed = Path.parse(path);
        return remove(parsed, options);
    }

    ResolvableConfig remove(Path path, ConfigRemoveOptions options) {
        expectNonNull(path, "path");
        MapConfigNode newRoot = root.remove(Path.root(), path, options);
        return withRoot(newRoot);
    }

    @Override
    public Config getSubConfig(String path) {
        expectNonBlank(path, "path");
        return getSubConfigOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    @Override
    public Config getSubConfigOrEmpty(String path) {
        expectNonBlank(path, "path");
        return getSubConfig(path, withRoot(MapConfigNode.emptyRoot()));
    }

    @Override
    public Config getSubConfig(String path, Config defaultValue) {
        expectNonBlank(path, "path");
        return getSubConfigOptional(path)
                .orElse(defaultValue);
    }

    @Override
    public Optional<Config> getSubConfigOptional(String path) {
        expectNonBlank(path, "path");
        return root.getOptionalNode(Path.parse(path))
                .filter(node -> node instanceof MapConfigNode)
                .map(node -> withRoot((MapConfigNode) node));
    }

    @Override
    public <T> Optional<List<T>> getListAsOptional(Class<T> type, String path) {
        return getOptional(path)
                .map(value -> value.getAsList(valueParser, type));
    }

    @Override
    public <T> Optional<T> getAsOptional(Class<T> type, String path) {
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

    public Map<String, Object> toFlatMap() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        root.entries().stream()
                .map(entry -> entry(entry.getKey().toString(), entry.getValue()))
                .sorted(comparingByKey())
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return unmodifiableMap(result);
    }
}
