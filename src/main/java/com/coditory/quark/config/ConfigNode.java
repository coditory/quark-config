package com.coditory.quark.config;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

interface ConfigNode {
    boolean isEmpty();

    Object unwrap();

    ConfigNode addIfMissing(Path parentPath, Path subPath, Object value);

    ConfigNode addOrReplace(Path parentPath, Path subPath, Object value);

    ConfigNode remove(Path parentPath, Path subPath, ConfigRemoveOptions options);

    ConfigNode withDefaults(ConfigNode root);

    ConfigNode filterLeaves(Path parentPath, ConfigEntryPredicate predicate, ConfigRemoveOptions options);

    ConfigNode mapLeaves(Path parentPath, ConfigEntryMapper mapper);

    default ConfigNode mapLeaves(Function<Object, Object> mapper) {
        return mapLeaves((path, value) -> mapper.apply(value));
    }

    default ConfigNode mapLeaves(ConfigEntryMapper mapper) {
        return mapLeaves(Path.root(), mapper);
    }

    default ConfigNode mapLeaves(Path parentPath, Function<Object, Object> mapper) {
        return mapLeaves(parentPath, (path, value) -> mapper.apply(value));
    }

    Optional<ConfigNode> getOptionalNode(Path subPath);

    default Optional<Object> getOptional(Path subPath) {
        return getOptionalNode(subPath)
                .map(ConfigNode::unwrap);
    }

    boolean anyLeaf(Predicate<Object> predicate);

    List<Entry<Path, Object>> entries();
}

