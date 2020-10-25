package com.coditory.configio;

import com.coditory.configio.api.InvalidConfigPathException;
import com.coditory.configio.api.ValueParser;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.coditory.configio.ConfigValueParser.DEFAULT_VALUE_PARSERS;
import static com.coditory.configio.ConfigValueParser.defaultConfigValueParser;
import static com.coditory.configio.MapConfigNode.emptyRoot;
import static com.coditory.configio.Path.root;
import static com.coditory.configio.Preconditions.expectNonBlank;
import static com.coditory.configio.Preconditions.expectNonNull;
import static com.coditory.configio.api.MissingConfigValueException.missingConfigValueForPath;

public class Config implements ConfigValueExtractor {
    private final static Config EMPTY = new Config(emptyRoot(), defaultConfigValueParser());

    public static Config empty() {
        return EMPTY;
    }

    public static Config of(Map<String, ?> values) {
        expectNonNull(values, "values");
        return builder()
                .withValues(values)
                .build();
    }

    public static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    private final ConfigValueParser valueParser;
    private final MapConfigNode root;

    private Config(MapConfigNode root, ConfigValueParser valueParser) {
        this.root = expectNonNull(root);
        this.valueParser = expectNonNull(valueParser);
    }

    public Map<String, Object> toMap() {
        return root.unwrap();
    }

    public Set<Entry<String, Object>> entries() {
        return root.entries();
    }

    public boolean contains(String path) {
        expectNonBlank(path, "path");
        return root.getOptionalNode(Path.parse(path))
                .isPresent();
    }

    public Config withDefault(String path, Object value) {
        expectNonBlank(path, "path");
        expectNonNull(value, "value");
        Path parsed = Path.parse(path);
        MapConfigNode newRoot = root.addIfMissing(root(), parsed, value);
        return withRoot(newRoot);
    }

    public Config withDefaults(Config config) {
        expectNonNull(config, "config");
        MapConfigNode mergedRoot = root.withDefaults(config.root);
        return withRoot(mergedRoot);
    }

    public Config withValue(String path, Object value) {
        expectNonBlank(path, "path");
        expectNonNull(value, "value");
        Path parsed = Path.parse(path);
        ConfigNode newRoot = root.addOrReplace(root(), parsed, value);
        if (!(newRoot instanceof MapConfigNode)) {
            throw new InvalidConfigPathException("Expected root node to be a map. Got: " + newRoot.getClass().getSimpleName());
        }
        return withRoot((MapConfigNode) newRoot);
    }

    public Config withValues(Config config) {
        expectNonNull(config, "config");
        MapConfigNode mergedRoot = config.root.withDefaults(this.root);
        return withRoot(mergedRoot);
    }

    public Config resolveExpressionsOrFail() {
        return resolveExpressionsOrFail(Config.empty());
    }

    public Config resolveExpressionsOrFail(Config variables) {
        expectNonNull(variables, "variables");
        return resolveExpressions(variables, Expression::failOnUnresolved);
    }

    public Config resolveExpressions() {
        return resolveExpressions(Config.empty());
    }

    public Config resolveExpressions(Config variables) {
        expectNonNull(variables, "variables");
        return resolveExpressions(variables, Expression::unwrap);
    }

    private Config resolveExpressions(Config variables, Function<Object, Object> leafMapper) {
        Config configWithExpressions = this.mapLeaves(ExpressionParser::parse);
        Config configWithExpressionsAndVariables = configWithExpressions
                .withDefaults(variables)
                .mapLeaves(ExpressionParser::parse);
        ExpressionResolver resolver = new ExpressionResolver(configWithExpressionsAndVariables);
        return this.mapLeaves(ExpressionParser::parse)
                .mapLeaves(resolver::resolve)
                .mapLeaves(leafMapper);
    }

    private Config withValueParser(ValueParser parser) {
        expectNonNull(parser, "parser");
        return withValueParser(valueParser.addParser(parser));
    }

    private Config withValueParser(ConfigValueParser newConfigValueParser) {
        return Objects.equals(newConfigValueParser, valueParser)
                ? this
                : new Config(root, newConfigValueParser);
    }

    private Config withValueParsers(List<ValueParser> parsers) {
        expectNonNull(parsers, "parsers");
        ConfigValueParser newValueParser = new ConfigValueParser(parsers);
        return Objects.equals(valueParser, newValueParser)
                ? this
                : new Config(root, valueParser);
    }

    private Config withRoot(MapConfigNode root) {
        return Objects.equals(this.root, root)
                ? this
                : new Config(root, valueParser);
    }

    private boolean anyLeaf(Predicate<Object> predicate) {
        return root.anyLeaf(predicate);
    }

    private Config mapLeaves(Function<Object, Object> mapper) {
        MapConfigNode mapped = root.mapLeaves(mapper);
        return withRoot(mapped);
    }

    MapConfigNode getRoot() {
        return root;
    }

    public Config remove(String path) {
        expectNonBlank(path, "path");
        Path parsed = Path.parse(path);
        MapConfigNode newRoot = root.remove(root(), parsed);
        return withRoot(newRoot);
    }

    public Config getSubConfig(String path) {
        expectNonBlank(path, "path");
        return getSubConfigOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    public Config getSubConfigOrEmpty(String path) {
        expectNonBlank(path, "path");
        return getSubConfig(path, withRoot(emptyRoot()));
    }

    public Config getSubConfig(String path, Config defaultValue) {
        expectNonBlank(path, "path");
        return getSubConfigOptional(path)
                .orElse(defaultValue);
    }

    public Optional<Config> getSubConfigOptional(String path) {
        expectNonBlank(path, "path");
        return root.getOptionalNode(Path.parse(path))
                .filter(node -> node instanceof MapConfigNode)
                .map(node -> new Config((MapConfigNode) node, valueParser));
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

    public boolean isEmpty() {
        return root.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return valueParser.equals(config.valueParser) &&
                root.equals(config.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueParser, root);
    }

    static class ConfigBuilder {
        private MapConfigNode root = emptyRoot();
        private List<ValueParser> valueParsers = new ArrayList<>(DEFAULT_VALUE_PARSERS);

        private ConfigBuilder() {
        }

        public ConfigBuilder withValueParser(ValueParser parser) {
            expectNonNull(parser, "parser");
            valueParsers.add(parser);
            return this;
        }

        public <T> ConfigBuilder withValueParser(Class<T> type, Function<String, T> parser) {
            expectNonNull(type, "type");
            expectNonNull(parser, "parser");
            return withValueParser(ValueParser.forType(type, parser));
        }

        public ConfigBuilder withValueParsers(List<ValueParser> parsers) {
            expectNonNull(parsers, "parsers");
            valueParsers = List.copyOf(parsers);
            return this;
        }

        public ConfigBuilder withValues(Map<String, ?> values) {
            expectNonNull(values, "values");
            values.forEach(this::withValue);
            return this;
        }

        public ConfigBuilder withValue(String name, Object value) {
            expectNonBlank(name, "name");
            if (value != null) {
                Path path = Path.parse(name);
                if (path.isRoot() || !path.getFirstElement().isNamed()) {
                    throw new InvalidConfigPathException(
                            "Expected non empty path to a named element. " +
                            "Example: a.b. Got: " + path);
                }
                root = (MapConfigNode) root.addOrReplace(root(), path, value);
            }
            return this;
        }

        public Config build() {
            return new Config(root, new ConfigValueParser(valueParsers));
        }
    }
}
