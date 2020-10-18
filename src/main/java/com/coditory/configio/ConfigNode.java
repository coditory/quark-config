package com.coditory.configio;

import com.coditory.configio.api.InvalidConfigPathException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

interface ConfigNode {
    static ConfigNode of(Path parentPath, Path path, Object value) {
        ConfigNode result = new LeafConfigNode(parentPath.add(path), value);
        if (path.isRoot()) {
            return result;
        }
        Path.PathElement element = path.getLastElement();
        Path currentPath = path.removeLastElement();
        while (!currentPath.isRoot()) {
            if (element.isNamed()) {
                result = new MapConfigNode(parentPath.add(currentPath), Map.of(element.getName(), result));
            } else if (element.isIndexed()) {
                int index = element.getIndex();
                if (index != 0) {
                    throw new InvalidConfigPathException("Expected index 0 on path: " + parentPath.add(path));
                }
                List<ConfigNode> values = new ArrayList<>();
                values.add(index, result);
                result = new ListConfigNode(parentPath.add(currentPath), values);
            } else {
                throw new InvalidConfigPathException("Unrecognized path element: " + element);
            }
            element = currentPath.getLastElement();
            currentPath = currentPath.removeLastElement();
        }
        return result;
    }

    Path path();

    boolean isEmpty();

    Object unwrap();

    Optional<ConfigNode> getOptionalNode(Path subPath);

    Object getOrThrow(Path subPath);

    ConfigNode addIfMissing(Path subPath, Object value);

    ConfigNode addOrThrow(Path subPath, Object value);

    ConfigNode addOrReplace(Path subPath, Object value);

    ConfigNode mapLeaves(Function<Object, Object> mapper);

    default Optional<Object> getOptional(Path subPath) {
        return getOptionalNode(subPath)
                .map(ConfigNode::unwrap);
    }
}

