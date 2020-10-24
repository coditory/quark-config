package com.coditory.configio;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

interface ConfigNode {
    boolean isEmpty();

    Object unwrap();

    ConfigNode addIfMissing(Path parentPath, Path subPath, Object value);

    ConfigNode addOrReplace(Path parentPath, Path subPath, Object value);

    ConfigNode remove(Path parentPath, Path subPath);

    ConfigNode withDefaults(ConfigNode root);

    ConfigNode mapLeaves(Function<Object, Object> mapper);

    Optional<ConfigNode> getOptionalNode(Path subPath);

    default Optional<Object> getOptional(Path subPath) {
        return getOptionalNode(subPath)
                .map(ConfigNode::unwrap);
    }

    boolean anyLeaf(Predicate<Object> predicate);

    Set<Entry<String, Object>> entries();
}

