package com.coditory.configio;

import com.coditory.configio.api.ConfigException;
import com.coditory.configio.api.ConfigParseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ConfigParser {
    public enum ConfigFormat {
        YAML(List.of("yml", "yaml"), new YamlConfigParser()),
        JSON(List.of("json"), new JsonConfigParser()),
        PROPERTIES(List.of("properties"), new PropertiesConfigParser());

        private final List<String> fileExtensions;
        private final ConfigFormatParser parser;

        ConfigFormat(List<String> fileExtensions, ConfigFormatParser parser) {
            this.fileExtensions = fileExtensions;
            this.parser = parser;
        }

        Config parse(String input) {
            try {
                return parser.parse(input);
            } catch (Exception e) {
                throw new ConfigParseException("Could not parse config", e);
            }
        }

        Config parse(InputStream inputStream) {
            try {
                return parser.parse(inputStream);
            } catch (Exception e) {
                throw new ConfigParseException("Could not parse config", e);
            }
        }

        String stringify(Config config) {
            return parser.stringify(config);
        }

        boolean filePathMatches(String filePath) {
            return fileExtensions.stream()
                    .anyMatch(ext -> filePath.endsWith("." + ext));
        }

        static ConfigFormat getFormatForFilePath(String filePath) {
            return stream(ConfigFormat.values())
                    .filter(format -> format.filePathMatches(filePath))
                    .findFirst()
                    .orElseThrow(() -> new ConfigException("Unrecognized config format for file path: " + filePath));
        }

        static boolean containsConfigExtension(String filePath) {
            return stream(ConfigFormat.values())
                    .anyMatch(format -> format.filePathMatches(filePath));
        }

        static List<String> getExtensions() {
            return stream(ConfigFormat.values())
                    .flatMap(format -> format.fileExtensions.stream())
                    .collect(toList());
        }
    }

    public static String toJson(Config config) {
        return stringify(ConfigFormat.JSON, config);
    }

    public static String toYAML(Config config) {
        return stringify(ConfigFormat.YAML, config);
    }

    public static String toProperties(Config config) {
        return stringify(ConfigFormat.PROPERTIES, config);
    }

    public static String stringify(ConfigFormat format, Config config) {
        return format.stringify(config);
    }

    public static Config parse(String filePath, InputStream inputStream) {
        return parse(ConfigFormat.getFormatForFilePath(filePath), inputStream);
    }

    public static Config parse(ConfigFormat format, InputStream inputStream) {
        return format.parse(inputStream);
    }
}

interface ConfigFormatParser {
    Config parse(String config) throws Exception;

    Config parse(InputStream config) throws Exception;

    String stringify(Config config);
}

class YamlConfigParser implements ConfigFormatParser {
    @Override
    public Config parse(String config) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(config);
        return Config.of(map);
    }

    @Override
    public Config parse(InputStream config) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(config);
        return Config.of(map);
    }

    @Override
    public String stringify(Config config) {
        Yaml yaml = new Yaml();
        return yaml.dump(config.toMap());
    }
}

class JsonConfigParser implements ConfigFormatParser {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Override
    @SuppressWarnings("unchecked")
    public Config parse(String config) {
        Map<String, Object> map = gson.fromJson(config, Map.class);
        return Config.of(map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Config parse(InputStream config) {
        Reader reader = new InputStreamReader(config);
        Map<String, Object> map = gson.fromJson(reader, Map.class);
        return Config.of(map);
    }

    @Override
    public String stringify(Config config) {
        return gson.toJson(config.toMap());
    }
}

class PropertiesConfigParser implements ConfigFormatParser {
    @Override
    public Config parse(String config) throws Exception {
        InputStream targetStream = new ByteArrayInputStream(config.getBytes());
        return parse(targetStream);
    }

    @Override
    public Config parse(InputStream config) throws Exception {
        Properties properties = new Properties();
        properties.load(config);
        if (properties.isEmpty()) {
            return Config.empty();
        }
        List<Map.Entry<Object, Object>> entries = new ArrayList<>(properties.entrySet());
        Map<String, Object> map = new LinkedHashMap<>();
        // properties are read in reversed order
        for (int i = entries.size() - 1; i >= 0; --i) {
            Map.Entry<Object, Object> entry = entries.get(i);
            String key = Objects.toString(entry.getKey());
            map.put(key, entry.getValue());
        }
        return Config.of(map);
    }

    @Override
    public String stringify(Config config) {
        return config.entries().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(joining("\n"));
    }
}
