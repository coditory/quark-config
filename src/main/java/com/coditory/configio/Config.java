package com.coditory.configio;

import com.coditory.configio.api.InvalidConfigPathException;
import com.coditory.configio.api.MissingConfigValueException;
import com.coditory.configio.api.ValueParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.coditory.configio.ConfigValueParser.DEFAULT_VALUE_PARSERS;
import static com.coditory.configio.ConfigValueParser.defaultValueParser;
import static com.coditory.configio.MapConfigNode.emptyRoot;
import static com.coditory.configio.Path.root;
import static com.coditory.configio.Preconditions.expectNonEmpty;
import static com.coditory.configio.Preconditions.expectNonNull;
import static com.coditory.configio.SystemEnvironmentNameMapper.mapSystemEnvironmentName;

public class Config implements ConfigValueExtractor {
    private final static Config EMPTY = new Config(emptyRoot(), defaultValueParser());

    public static Config empty() {
        return EMPTY;
    }

    public static Config of(Map<String, Object> values) {
        expectNonNull(values, "values");
        return builder()
                .withValues(values)
                .build();
    }

    public static Config fromArgs(String... args) {
        expectNonNull(args, "args");
        return builder()
                .withArguments(args)
                .build();
    }

    public static Config fromArgs(Map<String, String> aliases, String... args) {
        expectNonNull(args, "args");
        return builder()
                .withArguments(aliases, args)
                .build();
    }

    public static Config fromMap(Map<String, Object> values) {
        expectNonNull(values, "values");
        return builder()
                .withValues(values)
                .build();
    }

    public static Config fromSystemEnv() {
        return builder()
                .withSystemEnvValues()
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

    public Config copy() {
        return new Config(root, valueParser);
    }

    public Config subConfig(String path) {
        expectNonEmpty(path, "path");
        return getAsOptionalSubConfig(path)
                .orElseThrow(() -> new MissingConfigValueException("Could not get subConfig for path: " + path));
    }

    public Config subConfigOrEmpty(String path) {
        expectNonEmpty(path, "path");
        return getAsOptionalSubConfig(path)
                .orElseGet(() -> new Config(emptyRoot(), valueParser));
    }

    public boolean contains(String path) {
        expectNonEmpty(path, "path");
        return root.getOptionalNode(Path.parse(path))
                .isPresent();
    }

    public Config add(String path, Object value) {
        expectNonEmpty(path, "path");
        expectNonNull(value, "value");
        Path parsed = Path.parse(path);
        ConfigNode newRoot = root.addOrReplace(root(), parsed, value);
        if (!(newRoot instanceof MapConfigNode)) {
            throw new InvalidConfigPathException("Expected root node to be a map. Got: " + newRoot.getClass().getSimpleName());
        }
        return new Config((MapConfigNode) newRoot, valueParser);
    }

    public Config addDefault(String path, Object value) {
        expectNonEmpty(path, "path");
        expectNonNull(value, "value");
        Path parsed = Path.parse(path);
        MapConfigNode newRoot = root.addIfMissing(root(), parsed, value);
        return new Config(newRoot, valueParser);
    }

    public Config addDefaults(Config config) {
        expectNonNull(config, "config");
        MapConfigNode mergedRoot = root.withDefaults(config.root);
        return new Config(mergedRoot, valueParser);
    }

    public Config remove(String path) {
        expectNonEmpty(path, "path");
        Path parsed = Path.parse(path);
        MapConfigNode newRoot = root.remove(root(), parsed);
        return new Config(newRoot, valueParser);
    }

    private Optional<Config> getAsOptionalSubConfig(String path) {
        expectNonNull(path, "path");
        return root.getOptionalNode(Path.parse(path))
                .map(node ->
                    node instanceof MapConfigNode
                            ? new Config((MapConfigNode) node, valueParser)
                            : null
                );
    }

    @Override
    public <T> Optional<T> getAsOptional(Class<T> type, String path) {
        return getOptional(path)
                .map(value -> value.getAs(valueParser, type));
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

    private Optional<ConfigValue> getOptional(String path) {
        expectNonEmpty(path, "path");
        return getOptional(Path.parse(path));
    }

    @SuppressWarnings("unchecked")
    private Optional<ConfigValue> getOptional(Path path) {
        expectNonNull(path, "path");
        return root.getOptional(path)
                .map(value -> new ConfigValue(path, value));
    }

    public boolean isEmpty() {
        return root.isEmpty();
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

        public ConfigBuilder withArguments(String... args) {
            expectNonNull(args, "args");
            return withArguments(Map.of(), args);
        }

        public ConfigBuilder withArguments(Map<String, String> aliases, String... args) {
            expectNonNull(args, "args");
            expectNonNull(aliases, "aliases");
            Map<String, Object> values = new ArgumentsParser(aliases).parse(args);
            withValues(values);
            return this;
        }

        public ConfigBuilder withSystemEnvValues() {
            Map<String, String> mapped = System.getenv().entrySet().stream()
                    .collect(Collectors.toMap(e -> mapSystemEnvironmentName(e.getKey()), Map.Entry::getValue));
            withValues(mapped);
            return this;
        }

        public ConfigBuilder withValue(String name, Object value) {
            expectNonEmpty(name, "name");
            if (value != null) {
                Path path = Path.parse(name);
                root = root.addIfMissing(root(), path, value);
            }
            return this;
        }

        public Config build() {
            return new Config(root, new ConfigValueParser(valueParsers));
        }
    }
}
