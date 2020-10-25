package com.coditory.configio;

import com.coditory.configio.ConfigParser.ConfigFormat;
import com.coditory.configio.api.ConfigioException;
import com.coditory.configio.api.ConfigioParsingException;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.coditory.configio.ConfigLoader.ConfigSource.CLASSPATH;
import static com.coditory.configio.ConfigLoader.ConfigSource.FILE_SYSTEM;
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

        Config load(String path) {
            return configLoader.load(path);
        }
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

    public static Config loadFromClasspathInAnyFormat(String path) {
        return loadInAnyFormat(CLASSPATH, path);
    }

    public static Config loadFromClasspath(String path) {
        return load(CLASSPATH, path)
                .orElseThrow(() -> new ConfigioException(
                        "Could not load configuration. " +
                                "File was not found on classpath: " + path));
    }

    public static Config loadFromSystemFileInAnyFormat(String path) {
        return loadInAnyFormat(FILE_SYSTEM, path);
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
                .orElseThrow(() -> new ConfigioException(
                        "Could not load configuration. " +
                            "File was not found on file system: " + path));
    }

    private static Config loadInAnyFormat(ConfigSource configSource, String path) {
        List<String> filePathsWithExtensions = ConfigFormat.getExtensions().stream()
                .map(ext -> path + "." + ext)
                .collect(toList());
        List<String> filePaths = new ArrayList<>(filePathsWithExtensions.size() + 1);
        if (ConfigFormat.containsConfigExtension(path)) {
            filePaths.add(path);
        }
        filePaths.addAll(filePathsWithExtensions);
        return filePaths.stream()
                .map(configSource::load)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ConfigioException("Could not load configuration. " +
                        "Files were not found on " + configSource + ": " + filePaths));
    }

    private static Optional<Config> load(ConfigSource configSource, String path) {
        Config config = configSource.load(path);
        if (config == null) {
            throw new ConfigioException("Could not load configuration. " +
                    "File was not found on " + configSource + ": " + path);
        }
        return Optional.ofNullable(configSource.load(path));
    }
}

interface ConfigSourceLoader {
    Config load(String path);
}

class ClasspathConfigLoader implements ConfigSourceLoader {
    @Override
    public Config load(String path) {
        ConfigFormat format = ConfigFormat.getFormatForFilePath(path);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(path);
        if (url == null) {
            return null;
        }
        try {
            InputStream stream = url.openStream();
            return format.parse(stream);
        } catch (Exception e) {
            throw new ConfigioParsingException("Could not parse configuration from classpath file: " + path, e);
        }
    }
}

class FileSystemConfigLoader implements ConfigSourceLoader {
    @Override
    public Config load(String path) {
        ConfigFormat format = ConfigFormat.getFormatForFilePath(path);
        InputStream stream;
        try {
            stream = Files.newInputStream(Path.of(path));
        } catch (IOException e) {
            return null;
        }
        try {
            return format.parse(stream);
        } catch (Exception e) {
            throw new ConfigioParsingException("Could not parse configuration from file system: " + path, e);
        }
    }
}
