package com.coditory.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.coditory.config.ConfigNodeCreator.configNode;
import static com.coditory.config.ConfigNodeCreator.createNodeForValue;
import static java.util.Objects.requireNonNull;

class LeafConfigNode implements ConfigNode {
    private final Object value;

    LeafConfigNode(Object value) {
        this.value = requireNonNull(value);
    }

    @Override
    public Optional<ConfigNode> getOptionalNode(Path subPath) {
        return subPath.isRoot()
                ? Optional.of(this)
                : Optional.empty();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Object unwrap() {
        return value;
    }

    @Override
    public ConfigNode addOrReplace(Path parentPath, Path subPath, Object value) {
        return subPath.isRoot()
                ? new LeafConfigNode(value)
                : configNode(subPath, value);
    }

    @Override
    public ConfigNode remove(Path parentPath, Path subPath) {
        return this;
    }

    @Override
    public LeafConfigNode addIfMissing(Path parentPath, Path subPath, Object value) {
        return this;
    }

    @Override
    public ConfigNode mapLeaves(Path parentPath, ConfigValueMapper mapper) {
        Object mapped = mapper.mapValue(parentPath.toString(), value);
        return Objects.equals(mapped, value)
                ? this
                : createNodeForValue(mapped);
    }

    @Override
    public boolean anyLeaf(Predicate<Object> predicate) {
        return predicate.test(value);
    }

    @Override
    public List<Map.Entry<Path, Object>> entries() {
        return List.of(Map.entry(Path.root(), value));
    }

    @Override
    public LeafConfigNode withDefaults(ConfigNode other) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeafConfigNode that = (LeafConfigNode) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
