package com.coditory.quark.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;

class ArgumentsParser {
    private Map<String, String> aliases = new LinkedHashMap<>();
    private Map<String[], String[]> mapping = new LinkedHashMap<>();

    ArgumentsParser withMapping(Map<String[], String[]> argsMapping) {
        expectNonNull(argsMapping, "argsMapping");
        this.mapping = copy(argsMapping);
        return this;
    }

    ArgumentsParser addMapping(String[] args, String[] mapping) {
        expectNonNull(args, "args");
        expectNonNull(mapping, "mapping");
        this.mapping.put(copy(args), copy(mapping));
        return this;
    }

    ArgumentsParser withAliases(Map<String, String> argsAliases) {
        expectNonNull(argsAliases, "argsAliases");
        this.aliases = new LinkedHashMap<>(argsAliases);
        return this;
    }

    ArgumentsParser addAlias(String arg, String alias) {
        expectNonBlank(arg, "arg");
        expectNonBlank(alias, "alias");
        this.aliases.put(arg, alias);
        return this;
    }

    Map<String, Object> parse(String... args) {
        expectNonNull(args, "args");
        Map<String, Object> result = parseArgs(args);
        result = applyMapping(result);
        return result;
    }

    private Map<String, Object> parseArgs(String[] args) {
        Map<String, Object> result = new HashMap<>();
        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (arg.contains("=")) {
                    parseSwitchWithEquals(arg, result);
                } else if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    addArgument(result, arg, true);
                } else {
                    addArgument(result, arg, args[i + 1]);
                    i++;
                }
            }
            i++;
        }
        return result;
    }

    private Map<String, Object> applyMapping(Map<String, Object> result) {
        Map<String, Object> mapped = new LinkedHashMap<>(result);
        for (Entry<String[], String[]> entry : mapping.entrySet()) {
            Map<String, Object> keyMap = parseArgs(entry.getKey());
            Map<String, Object> valueMap = parseArgs(entry.getValue());
            for (Entry<String, Object> keyEntry : keyMap.entrySet()) {
                Object value = mapped.get(keyEntry.getKey());
                if (value != keyEntry.getValue()) {
                    mapped = new LinkedHashMap<>(result);
                    break;
                }
                mapped.putAll(valueMap);
            }
        }
        return mapped;
    }


    private void parseSwitchWithEquals(String arg, Map<String, Object> result) {
        int index = arg.indexOf("=");
        String key = arg.substring(0, index);
        String value = arg.substring(index + 1);
        addArgument(result, key, value);
    }

    private void addArgument(Map<String, Object> result, String key, Object value) {
        if (key.startsWith("--")) {
            result.put(key.substring(2), value);
        }
        String name = key.substring(1);
        if (aliases.containsKey(name)) {
            result.put(aliases.get(name), value);
        }
    }

    private String[] copy(String[] input) {
        return Arrays.copyOf(input, input.length);
    }

    private LinkedHashMap<String[], String[]> copy(Map<String[], String[]> input) {
        LinkedHashMap<String[], String[]> result = new LinkedHashMap<>();
        for (Map.Entry<String[], String[]> entry : input.entrySet()) {
            result.put(copy(entry.getKey()), copy(entry.getValue()));
        }
        return result;
    }
}