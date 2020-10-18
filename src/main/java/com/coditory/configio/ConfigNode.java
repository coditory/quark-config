package com.coditory.configio;

import com.coditory.configio.api.InvalidConfigPathException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

interface ConfigNode {
    static ConfigNode of(Path parentPath, Path path, Object value) {
        ConfigNode result = new LeafConfigNode(value);
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

    boolean isEmpty();

    Object unwrap();

    ConfigNode addIfMissing(Path parentPath, Path subPath, Object value);

    ConfigNode addOrThrow(Path parentPath, Path subPath, Object value);

    ConfigNode addOrReplace(Path parentPath, Path subPath, Object value);

    ConfigNode mapLeaves(Function<Object, Object> mapper);

    Optional<ConfigNode> getOptionalNode(Path subPath);

    default Optional<Object> getOptional(Path subPath) {
        return getOptionalNode(subPath)
                .map(ConfigNode::unwrap);
    }
}

