package com.coditory.quark.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.coditory.quark.config.ConfigValueParser.DEFAULT_VALUE_PARSERS;
import static com.coditory.quark.config.ConfigValueParser.defaultConfigValueParser;
import static com.coditory.quark.config.Preconditions.expect;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;
import static com.coditory.quark.config.MissingConfigValueException.missingConfigValueForPath;
import static com.coditory.quark.config.SecretHidingValueMapper.defaultSecretHidingValueMapper;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;

public class Config implements ConfigGetters {
    private final static Config EMPTY = new Config(
            MapConfigNode.emptyRoot(),
            defaultConfigValueParser(),
            defaultSecretHidingValueMapper()
    );

    public static Config empty() {
        return EMPTY;
    }

    public static Config of(String firstKey, Object firstValue, Object... otherEntries) {
        expectNonBlank(firstKey);
        expectNonNull(otherEntries);
        expect(otherEntries.length % 2 == 0, "Expected even entries. Got: ", 2 + otherEntries.length);
        Map<String, Object> entries = new LinkedHashMap<>();
        entries.put(firstKey, firstValue);
        for (int i = 0; i < otherEntries.length / 2; i+= 2) {
            expectNonNull(otherEntries[i], "config key");
            String key = Objects.toString(otherEntries[i]);
            entries.put(key, otherEntries[i + 1]);
        }
        return of(entries);
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
    private final ConfigValueMapper secretHidingValueMapper;

    private Config(
            MapConfigNode root,
            ConfigValueParser valueParser,
            ConfigValueMapper secretHidingValueMapper
    ) {
        this.root = expectNonNull(root);
        this.valueParser = expectNonNull(valueParser);
        this.secretHidingValueMapper = expectNonNull(secretHidingValueMapper);
    }

    public Map<String, Object> toMap() {
        return root.unwrap();
    }

    public List<Entry<String, Object>> entries() {
        return root.entries()
                .stream()
                .map(entry -> entry(entry.getKey().toString(), entry.getValue()))
                .collect(toList());
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
        MapConfigNode newRoot = root.addIfMissing(Path.root(), parsed, value);
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
        ConfigNode newRoot = root.addOrReplace(Path.root(), parsed, value);
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

    public Config resolveExpressions() {
        return resolveExpressions(Config.empty());
    }

    public Config resolveExpressions(Map<String, Object> values) {
        expectNonNull(values, "values");
        return resolveExpressions(Config.of(values), Expression::failOnUnresolved);
    }

    public Config resolveExpressions(Config values) {
        expectNonNull(values, "values");
        return resolveExpressions(values, Expression::failOnUnresolved);
    }

    public Config resolveExpressionsOrSkip() {
        return resolveExpressionsOrSkip(Config.empty());
    }

    public Config resolveExpressionsOrSkip(Config values) {
        expectNonNull(values, "values");
        return resolveExpressions(values, Expression::unwrap);
    }

    public Config resolveExpressionsOrSkip(Map<String, Object> values) {
        expectNonNull(values, "values");
        return resolveExpressions(Config.of(values), Expression::failOnUnresolved);
    }

    private Config resolveExpressions(Config variables, Function<Object, Object> leafMapper) {
        Config configWithExpressions = this.mapValues(ExpressionParser::parse);
        Config configWithExpressionsAndVariables = configWithExpressions
                .withDefaults(variables)
                .mapValues(ExpressionParser::parse);
        ExpressionResolver resolver = new ExpressionResolver(configWithExpressionsAndVariables);
        return this.mapValues(ExpressionParser::parse)
                .mapValues(resolver::resolve)
                .mapValues(leafMapper);
    }

    public Config withHiddenSecrets() {
        return mapValues(secretHidingValueMapper);
    }

    public Config withValueParser(ValueParser parser) {
        expectNonNull(parser, "parser");
        return withValueParser(valueParser.addParser(parser));
    }

    public Config withValueParsers(List<ValueParser> parsers) {
        expectNonNull(parsers, "parsers");
        ConfigValueParser newValueParser = new ConfigValueParser(parsers);
        return withValueParser(newValueParser);
    }

    private Config withValueParser(ConfigValueParser newConfigValueParser) {
        return Objects.equals(newConfigValueParser, valueParser)
                ? this
                : new Config(root, newConfigValueParser, secretHidingValueMapper);
    }

    private Config withRoot(MapConfigNode root) {
        return Objects.equals(this.root, root)
                ? this
                : new Config(root, valueParser, secretHidingValueMapper);
    }

    public Config mapValues(Function<Object, Object> mapper) {
        return mapValues((path, value) -> mapper.apply(value));
    }

    public Config mapValues(ConfigValueMapper mapper) {
        MapConfigNode mapped = root.mapLeaves(Path.root(), mapper);
        return withRoot(mapped);
    }

    MapConfigNode getRoot() {
        return root;
    }

    public Config remove(String path) {
        expectNonBlank(path, "path");
        Path parsed = Path.parse(path);
        MapConfigNode newRoot = root.remove(Path.root(), parsed);
        return withRoot(newRoot);
    }

    public Config getSubConfig(String path) {
        expectNonBlank(path, "path");
        return getSubConfigOptional(path)
                .orElseThrow(() -> missingConfigValueForPath(path));
    }

    public Config getSubConfigOrEmpty(String path) {
        expectNonBlank(path, "path");
        return getSubConfig(path, withRoot(MapConfigNode.emptyRoot()));
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
                .map(node -> withRoot((MapConfigNode) node));
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

    public boolean isNotEmpty() {
        return !isEmpty();
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

    @Override
    public String toString() {
        return withHiddenSecrets()
                .toMap()
                .toString();
    }

    public Map<String, Object> toFlatMap() {
        return root.entries().stream()
                .map(entry -> entry(entry.getKey().toString(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public static class ConfigBuilder {
        private MapConfigNode root = MapConfigNode.emptyRoot();
        private List<ValueParser> valueParsers = new ArrayList<>(DEFAULT_VALUE_PARSERS);
        private ConfigValueMapper secretHidingValueMapper = defaultSecretHidingValueMapper();

        private ConfigBuilder() {
        }

        public ConfigBuilder withValueParser(ValueParser parser) {
            expectNonNull(parser, "parser");
            List<ValueParser> valueParsers = new ArrayList<>(this.valueParsers);
            valueParsers.add(parser);
            this.valueParsers = List.copyOf(valueParsers);
            return this;
        }

        public <T> ConfigBuilder withValueParser(Class<T> type, Function<String, T> parser) {
            expectNonNull(type, "type");
            expectNonNull(parser, "parser");
            return withValueParser(ValueParser.forType(type, parser));
        }

        public ConfigBuilder withValueParsers(List<ValueParser> parsers) {
            expectNonNull(parsers, "parsers");
            this.valueParsers = List.copyOf(parsers);
            return this;
        }

        public ConfigBuilder withSecretHidingValueMapper(ConfigValueMapper secretHidingValueMapper) {
            this.secretHidingValueMapper = expectNonNull(secretHidingValueMapper, "secretHidingValueMapper");
            return this;
        }

        public ConfigBuilder withValues(Map<String, ?> values) {
            expectNonNull(values, "values");
            values.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(entry -> withValue(entry.getKey(), entry.getValue()));
            return this;
        }

        public ConfigBuilder withValues(Config config) {
            expectNonNull(config, "config");
            return withValues(config.toMap());
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
                this.root = (MapConfigNode) root.addOrReplace(Path.root(), path, value);
            }
            return this;
        }

        public Config build() {
            ConfigValueParser valueParser = new ConfigValueParser(valueParsers);
            return new Config(root, valueParser, secretHidingValueMapper);
        }
    }
}
