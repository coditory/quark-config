package com.coditory.configio;

import com.coditory.configio.api.InvalidConfigPathException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

class ConfigNodeCreator {
    static ConfigNode configNode(Path parentPath, Path path, Object value) {
        ConfigNode result = createNodeForValue(value);
        if (path.isRoot()) {
            return result;
        }
        Path.PathElement element = path.getLastElement();
        Path currentPath = path.removeLastElement();
        while (!currentPath.isRoot()) {
            if (element.isNamed()) {
                result = new MapConfigNode(Map.of(element.getName(), result));
            } else if (element.isIndexed()) {
                int index = element.getIndex();
                if (index != 0) {
                    throw new InvalidConfigPathException("Invalid path: " + parentPath + ". Expected index 0.");
                }
                List<ConfigNode> values = new ArrayList<>();
                values.add(index, result);
                result = new ListConfigNode(values);
            } else {
                throw new InvalidConfigPathException("Unrecognized path element: " + element);
            }
            element = currentPath.getLastElement();
            currentPath = currentPath.removeLastElement();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static ConfigNode createNodeForValue(Object value) {
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            boolean hasStringKeys = map.keySet().stream().allMatch(k -> k instanceof String);
            if (!hasStringKeys) {
                return new LeafConfigNode(value);
            }
            Map<String, Object> stringKeyMap = (Map<String, Object>) value;
            Map<String, ConfigNode> result = new HashMap<>(stringKeyMap.size());
            for (Map.Entry<String, Object> entry : stringKeyMap.entrySet()) {
                result.put(entry.getKey(), createNodeForValue(entry.getValue()));
            }
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
