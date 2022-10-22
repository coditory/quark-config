package com.coditory.quark.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coditory.quark.config.ConfigSource.CLASSPATH;
import static com.coditory.quark.config.ConfigSource.FILE_SYSTEM;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;

public final class ConfigFactory {
    public static Config buildFromSystemProperties() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            String rawKey = entry.getKey().toString();
            String key = rawKey.startsWith("java.version.")
                    ? rawKey.replaceFirst("java\\.version\\.", "java.")
                    : rawKey;
            result.put(key, entry.getValue());
        }
        return Config.of(result);
    }

    public static Config buildFromSystemEnvironment() {
        return Config.of(System.getenv());
    }

    public static Config buildFromArgs(String[] args) {
        expectNonNull(args, "args");
        return buildFromArgs(args, Map.of());
    }

    public static Config buildFromArgs(String[] args, Map<String, String> aliases) {
        expectNonNull(args, "args");
        expectNonNull(aliases, "aliases");
        return buildFromArgs(args, aliases, Map.of());
    }

    public static Config buildFromArgs(
            String[] args,
            Map<String, String> aliases,
            Map<String[], String[]> mapping
    ) {
        expectNonNull(args, "args");
        expectNonNull(aliases, "aliases");
        expectNonNull(mapping, "mapping");
        Map<String, Object> values = new ArgumentsParser()
                .withAliases(aliases)
                .withMapping(mapping)
                .parse(args);
        return Config.of(values);
    }

    public static Config loadFromClasspathOrEmpty(String path) {
        expectNonBlank(path, "path");
        return loadFromClasspathOrDefault(path, Config.empty());
    }

    public static Config loadFromClasspathOrDefault(String path, Config defaultConfig) {
        expectNonBlank(path, "path");
        expectNonNull(defaultConfig, "defaultConfig");
        return load(CLASSPATH, path)
                .orElse(defaultConfig);
    }

    public static Config loadFromClasspath(String path) {
        expectNonBlank(path, "path");
        return load(CLASSPATH, path)
                .orElseThrow(() -> new ConfigLoadException(
                        "Configuration file not found on classpath: " + path));
    }

    public static Config loadFromFileSystemOrEmpty(String path) {
        expectNonBlank(path, "path");
        return loadFromFileSystemOrDefault(path, Config.empty());
    }

    public static Config loadFromFileSystemOrDefault(String path, Config defaultConfig) {
        expectNonBlank(path, "path");
        expectNonNull(defaultConfig, "defaultConfig");
        return load(FILE_SYSTEM, path)
                .orElse(defaultConfig);
    }

    public static Config loadFromFileSystem(String path) {
        expectNonBlank(path, "path");
        return load(FILE_SYSTEM, path)
                .orElseThrow(() -> new ConfigLoadException(
                        "Configuration file not found on file system: " + path));
    }

    public static Config parseJson(String json) {
        expectNonNull(json, "json");
        return ConfigFormat.JSON.parse(json);
    }

    public static Config parseYaml(String yaml) {
        expectNonNull(yaml, "yaml");
        return ConfigFormat.YAML.parse(yaml);
    }

    public static Config parseProperties(String properties) {
        expectNonNull(properties, "properties");
        return ConfigFormat.PROPERTIES.parse(properties);
    }

    private static Optional<Config> load(ConfigSource configSource, String path) {
        return ConfigFormat.containsConfigExtension(path)
                ? configSource.load(path)
                : loadInAnyFormat(configSource, path);
    }

    private static Optional<Config> loadInAnyFormat(ConfigSource configSource, String path) {
        List<String> filePathsWithExtensions = ConfigFormat.getExtensions().stream()
                .map(ext -> path + "." + ext)
                .toList();
        return filePathsWithExtensions.stream()
                .map(configSource::load)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}