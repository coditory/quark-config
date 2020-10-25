package com.coditory.configio;

import com.coditory.configio.ConfigParser.ConfigFormat;
import com.coditory.configio.api.ConfigException;
import com.coditory.configio.api.ConfigLoadException;
import com.coditory.configio.api.ConfigParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coditory.configio.ConfigLoader.ConfigSource.CLASSPATH;
import static com.coditory.configio.ConfigLoader.ConfigSource.FILE_SYSTEM;
import static com.coditory.configio.Preconditions.expectNonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class ConfigLoader {
    enum ConfigSource {
        CLASSPATH(new ClasspathConfigLoader()),
        FILE_SYSTEM(new FileSystemConfigLoader());

        private final ConfigSourceLoader configLoader;

        ConfigSource(ConfigSourceLoader configLoader) {
            this.configLoader = requireNonNull(configLoader);
        }

        Optional<Config> load(String path) {
            return configLoader.load(path);
        }
    }

    public static ApplicationConfigLoader applicationConfig() {
        return new ApplicationConfigLoader();
    }

    public static Config loadSystemProperties() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry: System.getProperties().entrySet()) {
            String rawKey = entry.getKey().toString();
            String key = rawKey.startsWith("java.version.")
                    ? rawKey.replaceFirst("java\\.version\\.", "java.")
                    : rawKey;
            result.put(key, entry.getValue());
        }
        return Config.of(result);
    }

    public static Config loadSystemEnvironment() {
        return Config.of(System.getenv());
    }

    public static Config loadFromArgs(String[] args) {
        expectNonNull(args, "args");
        return loadFromArgs(args, Map.of());
    }

    public static Config loadFromArgs(String[] args, Map<String, String> aliases) {
        expectNonNull(args, "args");
        expectNonNull(aliases, "aliases");
        Map<String, Object> values = new ArgumentsParser(aliases).parse(args);
        return Config.of(values);
    }

    public static Config loadFromClasspathOrEmpty(String path) {
        return loadFromClasspathOrDefault(path, Config.empty());
    }

    public static Config loadFromClasspathOrDefault(String path, Config defaultConfig) {
        return load(CLASSPATH, path)
                .orElse(defaultConfig);
    }

    public static Config loadFromClasspath(String path) {
        return load(CLASSPATH, path)
                .orElseThrow(() -> new ConfigLoadException(
                        "Configuration file not found on classpath: " + path));
    }

    public static Config loadFromFileSystemOrEmpty(String path) {
        return loadFromFileSystemOrDefault(path, Config.empty());
    }

    public static Config loadFromFileSystemOrDefault(String path, Config defaultConfig) {
        return load(FILE_SYSTEM, path)
                .orElse(defaultConfig);
    }

    public static Config loadFromFileSystem(String path) {
        return load(FILE_SYSTEM, path)
                .orElseThrow(() -> new ConfigLoadException(
                        "Configuration file not found on file system: " + path));
    }

    private static Optional<Config> load(ConfigSource configSource, String path) {
        return ConfigFormat.containsConfigExtension(path)
            ? configSource.load(path)
            : loadInAnyFormat(configSource, path);
    }

    private static Optional<Config> loadInAnyFormat(ConfigSource configSource, String path) {
        List<String> filePathsWithExtensions = ConfigFormat.getExtensions().stream()
                .map(ext -> path + "." + ext)
                .collect(toList());
        return filePathsWithExtensions.stream()
                .map(configSource::load)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}

interface ConfigSourceLoader {
    Optional<Config> load(String path);
}

class ClasspathConfigLoader implements ConfigSourceLoader {
    @Override
    public Optional<Config> load(String path) {
        ConfigFormat format = ConfigFormat.getFormatForFilePath(path);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(path);
        if (url == null) {
            return Optional.empty();
        }
        try {
            InputStream stream = url.openStream();
            return Optional.of(format.parse(stream));
        } catch (Exception e) {
            throw new ConfigParseException("Could not parse configuration from classpath file: " + path, e);
        }
    }
}

class FileSystemConfigLoader implements ConfigSourceLoader {
    @Override
    public Optional<Config> load(String path) {
        ConfigFormat format = ConfigFormat.getFormatForFilePath(path);
        InputStream stream;
        try {
            stream = Files.newInputStream(Path.of(path));
        } catch (IOException e) {
            return Optional.empty();
        }
        try {
            return Optional.of(format.parse(stream));
        } catch (Exception e) {
            throw new ConfigParseException("Could not parse configuration from file system: " + path, e);
        }
    }
}
