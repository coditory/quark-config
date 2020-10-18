package com.coditory.configio;

import com.coditory.configio.api.MissingConfigValueException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

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
                : ConfigNode.of(parentPath, subPath, value);
    }

    @Override
    public LeafConfigNode addOrThrow(Path parentPath, Path subPath, Object value) {
        throw new MissingConfigValueException(
                "Could not add element on: " + parentPath.add(subPath) +
                        ". Got a leaf node on: " + parentPath
        );
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
