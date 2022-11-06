package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coditory.quark.config.ConfigSource.CLASSPATH;
import static com.coditory.quark.config.ConfigSource.FILE_SYSTEM;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;

public final class ConfigFactory {
    @NotNull
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

    @NotNull
    public static Config buildFromSystemEnvironment() {
        return Config.of(System.getenv());
    }

    @NotNull
    public static Config buildFromArgs(@NotNull String[] args) {
        expectNonNull(args, "args");
        return buildFromArgs(args, Map.of());
    }

    @NotNull
    public static Config buildFromArgs(@NotNull String[] args, @NotNull Map<String, String> aliases) {
        expectNonNull(args, "args");
        expectNonNull(aliases, "aliases");
        return buildFromArgs(args, aliases, Map.of());
    }

    @NotNull
    public static Config buildFromArgs(
            @NotNull String[] args,
            @NotNull Map<String, String> aliases,
            @NotNull Map<String[], String[]> mapping
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

    @NotNull
    public static Config loadFromClasspathOrEmpty(@NotNull String path) {
        expectNonBlank(path, "path");
        return loadFromClasspathOrDefault(path, Config.empty());
    }

    @NotNull
    public static Config loadFromClasspathOrDefault(@NotNull String path,@NotNull  Config defaultConfig) {
        expectNonBlank(path, "path");
        expectNonNull(defaultConfig, "defaultConfig");
        return load(CLASSPATH, path)
                .orElse(defaultConfig);
    }

    @NotNull
    public static Config loadFromClasspath(@NotNull String path) {
        expectNonBlank(path, "path");
        return load(CLASSPATH, path)
                .orElseThrow(() -> new ConfigLoadException(
                        "Configuration file not found on classpath: " + path));
    }

    @NotNull
    public static Config loadFromFileSystemOrEmpty(@NotNull String path) {
        expectNonBlank(path, "path");
        return loadFromFileSystemOrDefault(path, Config.empty());
    }

    @NotNull
    public static Config loadFromFileSystemOrDefault(@NotNull String path, @NotNull Config defaultConfig) {
        expectNonBlank(path, "path");
        expectNonNull(defaultConfig, "defaultConfig");
        return load(FILE_SYSTEM, path)
                .orElse(defaultConfig);
    }

    @NotNull
    public static Config loadFromFileSystem(@NotNull String path) {
        expectNonBlank(path, "path");
        return load(FILE_SYSTEM, path)
                .orElseThrow(() -> new ConfigLoadException(
                        "Configuration file not found on file system: " + path));
    }

    @NotNull
    public static Config parseJson(@NotNull String json) {
        expectNonNull(json, "json");
        return ConfigFormat.JSON.parse(json);
    }

    @NotNull
    public static Config parseYaml(@NotNull String yaml) {
        expectNonNull(yaml, "yaml");
        return ConfigFormat.YAML.parse(yaml);
    }

    @NotNull
    public static Config parseProperties(@NotNull String properties) {
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