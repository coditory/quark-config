package com.coditory.configio;

import com.coditory.configio.api.MissingConfigValueException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

class ListConfigNode implements ConfigNode {
    private final Path path;
    private final List<ConfigNode> values;

    ListConfigNode(Path path, List<ConfigNode> values) {
        this.path = requireNonNull(path);
        this.values = values;
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public Object unwrap() {
        return values.stream()
                .map(ConfigNode::unwrap)
                .collect(toList());
    }

    @Override
    public Optional<ConfigNode> getOptionalNode(Path subPath) {
        if (subPath.isRoot()) {
            return Optional.of(this);
        }
        return getChild(subPath.getFirstElement())
                .flatMap(child -> child.getOptionalNode(subPath.removeFirstElement()));
    }

    @Override
    public Object getOrThrow(Path subPath) {
        if (subPath.isRoot()) {
            return unwrap();
        }
        return getChild(subPath.getFirstElement())
                .orElseThrow(() -> {
                    String message = "Could not get element for path: " + path +
                            ". Got list on path: " + this.path + " with size: " + values.size();
                    return new MissingConfigValueException(message);
                });
    }

    @Override
    public ListConfigNode addIfMissing(Path subPath, Object value) {
        if (subPath.isRoot()) {
            return this;
        }
        ConfigNode child = getChild(subPath.getFirstElement())
                .map(c -> c.addIfMissing(subPath.removeFirstElement(), value))
                .orElseGet(() -> ConfigNode.of(path.add(subPath.getFirstElement()), subPath.removeFirstElement(), value));
        return addOrReplaceChild(subPath.getFirstElement(), child);
    }

    @Override
    public ListConfigNode addOrThrow(Path subPath, Object value) {
        if (subPath.isRoot()) {
            return this;
        }
        return getChild(subPath.getFirstElement())
                .map(c -> c.addOrThrow(subPath.removeFirstElement(), value))
                .map(c -> this.addOrReplaceChild(subPath.getFirstElement(), c))
                .orElseThrow(() -> {
                    String message = "Could not add element on path: " + path.add(subPath) +
                            ". Got list on path: " + this.path + " with size: " + values.size();
                    return new MissingConfigValueException(message);
                });
    }

    @Override
    public ConfigNode addOrReplace(Path subPath, Object value) {
        if (subPath.isRoot()) {
            return new LeafConfigNode(path, value);
        }
        return getChild(subPath.getFirstElement())
                .map(c -> c.addOrReplace(subPath.removeFirstElement(), value))
                .map(c -> this.addOrReplaceChild(subPath.getFirstElement(), c))
                .orElse(this);
    }

    private ListConfigNode addOrReplaceChild(Path.PathElement element, ConfigNode node) {
        int index = element.getIndex();
        List<ConfigNode> children = new ArrayList<>(this.values);
        children.add(index, node);
        return new ListConfigNode(path, children);
    }

    private Optional<ConfigNode> getChild(Path.PathElement element) {
        if (!element.isIndexed()) {
            return Optional.empty();
        }
        int index = element.getIndex();
        if (index >= values.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(values.get(index));
    }

    @Override
    public ConfigNode mapLeaves(Function<Object, Object> mapper) {
        List<ConfigNode> mapped = values.stream()
                .map(child -> child.mapLeaves(mapper))
                .collect(toList());
        return new ListConfigNode(path, mapped);
    }
}
