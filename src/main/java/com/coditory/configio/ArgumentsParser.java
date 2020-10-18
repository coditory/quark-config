package com.coditory.configio;

import java.util.HashMap;
import java.util.Map;

import static com.coditory.configio.Preconditions.expectNonNull;

class ArgumentsParser {
    private final Map<String, String> aliases;

    ArgumentsParser() {
        this(Map.of());
    }

    ArgumentsParser(Map<String, String> aliases) {
        this.aliases = Map.copyOf(aliases);
    }

    public final Map<String, Object> parse(String... args) {
        expectNonNull(args, "args");
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
}