package com.coditory.quark.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.coditory.quark.config.ConfigRemoveOptions.leaveEmptyParents;
import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParents;
import static com.coditory.quark.config.ConfigValueParser.DEFAULT_VALUE_PARSERS;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;
import static com.coditory.quark.config.SecretHidingValueMapper.defaultSecretHidingValueMapper;

public class ConfigBuilder {
    private MapConfigNode root = MapConfigNode.emptyRoot();
    private ConfigValueParser valueParser = new ConfigValueParser(DEFAULT_VALUE_PARSERS);
    private ConfigEntryMapper secretHidingValueMapper = defaultSecretHidingValueMapper();

    ConfigBuilder() {
        this(Map.of());
    }

    ConfigBuilder(Map<String, ?> values) {
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    ConfigBuilder setRootNode(MapConfigNode root) {
        expectNonNull(root, "root");
        this.root = root;
        return this;
    }

    public ConfigBuilder addValueParser(ValueParser parser) {
        expectNonNull(parser, "parser");
        this.valueParser = this.valueParser.addParser(parser);
        return this;
    }

    public <T> ConfigBuilder addValueParser(Class<T> type, Function<String, T> parser) {
        expectNonNull(type, "type");
        expectNonNull(parser, "parser");
        return addValueParser(ValueParser.forType(type, parser));
    }

    public ConfigBuilder setValueParser(ValueParser parser) {
        expectNonNull(parser, "parser");
        return setValueParsers(List.of(parser));
    }

    public ConfigBuilder setValueParsers(List<ValueParser> parsers) {
        expectNonNull(parsers, "parsers");
        this.valueParser = new ConfigValueParser(parsers);
        return this;
    }

    ConfigBuilder setValueParser(ConfigValueParser parser) {
        expectNonNull(parser, "parser");
        this.valueParser = parser;
        return this;
    }

    public ConfigBuilder setSecretHidingValueMapper(ConfigEntryMapper secretHidingValueMapper) {
        this.secretHidingValueMapper = expectNonNull(secretHidingValueMapper, "secretHidingValueMapper");
        return this;
    }

    public ConfigBuilder putAll(Map<String, ?> values) {
        expectNonNull(values, "values");
        return putAll(Config.of(values));
    }

    public ConfigBuilder putAll(Config config) {
        expectNonNull(config, "config");
        this.root = config.getRootNode().withDefaults(root);
        return this;
    }

    public ConfigBuilder put(String name, Object value) {
        expectNonBlank(name, "name");
        if (value != null) {
            Path path = Path.parseAbsolute(name);
            this.root = (MapConfigNode) root.addOrReplace(Path.root(), path, value);
        }
        return this;
    }

    public ConfigBuilder putAllIfMissing(Map<String, Object> values) {
        expectNonNull(values, "values");
        return putAllIfMissing(Config.of(values));
    }

    public ConfigBuilder putAllIfMissing(Config config) {
        expectNonNull(config, "config");
        this.root = root.withDefaults(config.getRootNode());
        return this;
    }

    public ConfigBuilder putIfMissing(String name, Object value) {
        expectNonBlank(name, "name");
        if (value != null) {
            Path path = Path.parseAbsolute(name);
            this.root = root.addIfMissing(Path.root(), path, value);
        }
        return this;
    }

    public ConfigBuilder resolveExpressions() {
        return resolveExpressions(Config.empty());
    }

    public ConfigBuilder resolveExpressions(Map<String, Object> values) {
        expectNonNull(values, "values");
        return resolveExpressions(Config.of(values));
    }

    public ConfigBuilder resolveExpressions(Config config) {
        expectNonNull(config, "config");
        return resolveExpressions(config, Expression::failOnUnresolved);
    }

    public ConfigBuilder resolveExpressionsOrSkip() {
        return resolveExpressionsOrSkip(Config.empty());
    }

    public ConfigBuilder resolveExpressionsOrSkip(Map<String, Object> values) {
        expectNonNull(values, "values");
        return resolveExpressionsOrSkip(Config.of(values));
    }

    public ConfigBuilder resolveExpressionsOrSkip(Config config) {
        expectNonNull(config, "config");
        return resolveExpressions(config, Expression::unwrap);
    }

    private ConfigBuilder resolveExpressions(Config variables, Function<Object, Object> leafMapper) {
        MapConfigNode rootWithExpressions = root.mapLeaves(ExpressionParser::parse);
        MapConfigNode rootWithExpressionsAndVariables = variables.getRootNode()
                .withDefaults(rootWithExpressions)
                .mapLeaves(Path.root(), (path, value) -> ExpressionParser.parse(value));
        Config resolutionConfig = new ResolvableConfig(rootWithExpressionsAndVariables, valueParser, secretHidingValueMapper);
        ExpressionResolver resolver = new ExpressionResolver(resolutionConfig);
        root = root.mapLeaves(ExpressionParser::parse)
                .mapLeaves(resolver::resolve)
                .mapLeaves(leafMapper);
        return this;
    }

    public ConfigBuilder removeEmptyProperties() {
        ConfigEntryPredicate predicate = (path, value) -> {
            if (value instanceof List<?> && ((List<?>) value).isEmpty()) {
                return false;
            } else if (value instanceof Map<?, ?> && ((Map<?, ?>) value).isEmpty()) {
                return false;
            }
            return true;
        };
        return filterValues(predicate, removeEmptyParents());
    }

    public ConfigBuilder filterValues(ConfigEntryPredicate predicate, ConfigRemoveOptions options) {
        MapConfigNode mapped = root.filterLeaves(Path.root(), predicate, options);
        root = mapped == null ? MapConfigNode.emptyRoot() : mapped;
        return this;
    }

    public ConfigBuilder mapValues(Function<Object, Object> mapper) {
        return mapValues((path, value) -> mapper.apply(value));
    }

    public ConfigBuilder mapValues(ConfigEntryMapper mapper) {
        root = root.mapLeaves(Path.root(), mapper);
        return this;
    }

    public ConfigBuilder remove(String path) {
        return remove(path, leaveEmptyParents());
    }

    public ConfigBuilder remove(String path, ConfigRemoveOptions options) {
        expectNonBlank(path, "path");
        Path parsed = Path.parse(path);
        return remove(parsed, options);
    }

    private ConfigBuilder remove(Path path, ConfigRemoveOptions options) {
        expectNonNull(path, "path");
        MapConfigNode mapped = root.remove(Path.root(), path, options);
        root = mapped == null ? MapConfigNode.emptyRoot() : mapped;
        return this;
    }

    public Config build() {
        return new ResolvableConfig(root, valueParser, secretHidingValueMapper);
    }
}
