package com.coditory.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class ConfigNodeCreator {
    static ConfigNode configNode(Path path, Object value) {
        ConfigNode result = createNodeForValue(value);
        if (path.isRoot()) {
            return result;
        }
        Path currentPath = path;
        while (!currentPath.isRoot()) {
            Path.PathElement element = currentPath.getLastElement();
            if (element.isNamed()) {
                result = new MapConfigNode(Map.of(element.getName(), result));
            } else if (element.isIndexed()) {
                int index = element.getIndex();
                if (index != 0) {
                    throw new InvalidConfigPathException("First list element must start with index 0. Got: " + index);
                }
                List<ConfigNode> values = new ArrayList<>();
                values.add(index, result);
                result = new ListConfigNode(values);
            } else {
                throw new InvalidConfigPathException("Unrecognized path element: " + element);
            }
            currentPath = currentPath.removeLastElement();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    static ConfigNode createNodeForValue(Object value) {
        if (value instanceof Config) {
            Config config = (Config) value;
            return config.getRoot();
        }
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            boolean hasStringKeys = map.keySet().stream().allMatch(k -> k instanceof String);
            if (!hasStringKeys) {
                return new LeafConfigNode(value);
            }
            Map<String, Object> stringKeyMap = (Map<String, Object>) value;
            Map<String, ConfigNode> result = stringKeyMap.entrySet().stream()
                    .sorted(Entry.comparingByKey())
                    .map(entry -> {
                        Path path = Path.parse(entry.getKey());
                        ConfigNode child = configNode(path.removeFirstElement(), entry.getValue());
                        return Map.entry(path.getFirstElement().getName(), child);
                    })
                    .collect(toMap(Entry::getKey, Entry::getValue));
            return new MapConfigNode(result);
        }
        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            List<ConfigNode> result = list.stream()
                    .map(ConfigNodeCreator::createNodeForValue)
                    .collect(toList());
            return new ListConfigNode(result);
        }
        return new LeafConfigNode(value);
    }
}
