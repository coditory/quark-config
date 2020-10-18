package com.coditory.configio;

import com.coditory.configio.api.MissingConfigValueException;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

class LeafConfigNode implements ConfigNode {
    private final Path path;
    private final Object value;

    LeafConfigNode(Path path, Object value) {
        this.path = requireNonNull(path);
        this.value = requireNonNull(value);
    }

    @Override
    public Optional<ConfigNode> getOptionalNode(Path subPath) {
        return subPath.isRoot()
                ? Optional.of(this)
                : Optional.empty();
    }

    @Override
    public Object getOrThrow(Path subPath) {
        if (subPath.isRoot()) {
            return value;
        }
        String message = "Could not resolve value for path: " + this.path.add(subPath) +
                ". Got leaf value on: " + this.path;
        throw new MissingConfigValueException(message);
    }

    @Override
    public ConfigNode addOrReplace(Path subPath, Object value) {
        return subPath.isRoot()
                ? new LeafConfigNode(path, value)
                : ConfigNode.of(path, subPath, value);
    }

    @Override
    public Path path() {
        return path;
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
    public LeafConfigNode addOrThrow(Path subPath, Object value) {
        String message = "Could not add element on path: " + subPath +
                ". Got leaf value on: " + this.path;
        throw new MissingConfigValueException(message);
    }

    @Override
    public LeafConfigNode addIfMissing(Path subPath, Object value) {
        return this;
    }

    @Override
    public ConfigNode mapLeaves(Function<Object, Object> mapper) {
        Object mapped = mapper.apply(value);
        return new LeafConfigNode(path, mapped);
    }
}
