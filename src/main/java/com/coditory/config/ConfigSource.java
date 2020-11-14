package com.coditory.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

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


    interface ConfigSourceLoader {
        Optional<Config> load(String path);
    }

    private static class ClasspathConfigLoader implements ConfigSourceLoader {
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

    private static class FileSystemConfigLoader implements ConfigSourceLoader {
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
}