package com.coditory.configio;

import com.coditory.configio.api.MissingConfigValueException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.coditory.configio.ConfigNodeCreator.configNode;
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
                : configNode(parentPath, subPath, value);
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
    public ConfigNode mapLeaves(Function<Object, Object> mapper) {
        Object mapped = mapper.apply(value);
        return new LeafConfigNode(mapped);
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
