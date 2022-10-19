package com.coditory.quark.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.coditory.quark.config.ConfigRemoveOptions.leaveEmptyParents;
import static com.coditory.quark.config.ConfigValueParser.DEFAULT_VALUE_PARSERS;
import static com.coditory.quark.config.Preconditions.expect;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;
import static com.coditory.quark.config.SecretHidingValueMapper.defaultSecretHidingValueMapper;

public interface Config extends ConfigGetters {
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

    static Config of(Map<String, ?> values) {
        expectNonNull(values, "values");
        return builder()
                .withValues(values)
                .build();
    }

    static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    Map<String, Object> toMap();

    Map<String, Object> toFlatMap();

    boolean contains(String path);

    Config withDefault(String path, Object value);

    Config withDefaults(Config config);

    Config withValue(String path, Object value);

    Config withValues(Config config);

    Config filterValues(ConfigEntryPredicate predicate, ConfigRemoveOptions options);

    default Config filterValues(ConfigEntryPredicate predicate) {
        return filterValues(predicate, leaveEmptyParents());
    }

    default Config filterValues(Predicate<Object> predicate) {
        return filterValues((path, value) -> predicate.test(Map.entry(path, value)), leaveEmptyParents());
    }

    default Config filterValues(Predicate<Object> predicate, ConfigRemoveOptions options) {
        return filterValues((path, value) -> predicate.test(Map.entry(path, value)), options);
    }

    Config mapValues(ConfigEntryMapper mapper);

    default Config mapValues(Function<Object, Object> mapper) {
        return mapValues((path, value) -> mapper.apply(value));
    }

    default Config remove(String path) {
        return remove(path, leaveEmptyParents());
    }

    Config remove(String path, ConfigRemoveOptions options);

    Config removeEmptyProperties();

    Config getSubConfig(String path);

    Config getSubConfigOrEmpty(String path);

    Config getSubConfig(String path, Config defaultValue);

    Optional<Config> getSubConfigOptional(String path);

    ConfigNode getRootNode();

    boolean isEmpty();

    default Config copy() {
        return Config.empty().withValues(this);
    }

    Config withHiddenSecrets();

    class ConfigBuilder {
        private MapConfigNode root = MapConfigNode.emptyRoot();
        private List<ValueParser> valueParsers = new ArrayList<>(DEFAULT_VALUE_PARSERS);
        private ConfigEntryMapper secretHidingValueMapper = defaultSecretHidingValueMapper();
        private Function<ResolvableConfig, ResolvableConfig> expressionResolver = null;
        private boolean removeEmptyProperties = false;

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

        public ConfigBuilder withSecretHidingValueMapper(ConfigEntryMapper secretHidingValueMapper) {
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

        public ConfigBuilder withResolvedExpressions() {
            return withResolvedExpressions(Config.empty());
        }

        public ConfigBuilder withResolvedExpressions(Map<String, Object> values) {
            expectNonNull(values, "values");
            return withResolvedExpressions(Config.of(values));
        }

        public ConfigBuilder withResolvedExpressions(Config config) {
            expectNonNull(config, "config");
            expressionResolver = (result) -> result.resolveExpressions(config);
            return this;
        }

        public ConfigBuilder withResolvedExpressionsOrSkip() {
            return withResolvedExpressionsOrSkip(Config.empty());
        }

        public ConfigBuilder withResolvedExpressionsOrSkip(Map<String, Object> values) {
            expectNonNull(values, "values");
            return withResolvedExpressionsOrSkip(Config.of(values));
        }

        public ConfigBuilder withResolvedExpressionsOrSkip(Config config) {
            expectNonNull(config, "config");
            expressionResolver = (result) -> result.resolveExpressionsOrSkip(config);
            return this;
        }

        public ConfigBuilder withoutEmptyProperties() {
            removeEmptyProperties = true;
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
                this.root = (MapConfigNode) root.addOrReplace(Path.root(), path, value);
            }
            return this;
        }

        public Config build() {
            ConfigValueParser valueParser = new ConfigValueParser(valueParsers);
            ResolvableConfig config = new ResolvableConfig(root, valueParser, secretHidingValueMapper);
            if (removeEmptyProperties) {
                config = config.removeEmptyProperties();
            }
            if (expressionResolver != null) {
                config = expressionResolver.apply(config);
            }
            return config;
        }

        public AuditableConfig buildAuditableConfig() {
            Config config = build();
            return new AuditableConfig(config);
        }
    }
}
