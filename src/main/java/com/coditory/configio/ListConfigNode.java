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
    private final List<ConfigNode> values;

    ListConfigNode(List<ConfigNode> values) {
        this.values = values;
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
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
    public ListConfigNode addIfMissing(Path parentPath, Path subPath, Object value) {
        if (subPath.isRoot()) {
            return this;
        }
        Path.PathElement element = subPath.getFirstElement();
        if (element.isNamed() || element.getIndex() > values.size()) {
            return this;
        }
        ConfigNode child = getChild(element)
                .map(c -> c.addIfMissing(parentPath.add(element), subPath.removeFirstElement(), value))
                .orElseGet(() -> ConfigNode.of(parentPath.add(element), subPath.removeFirstElement(), value));
        return addOrReplaceChild(element, child);
    }

    @Override
    public ListConfigNode addOrThrow(Path parentPath, Path subPath, Object value) {
        if (subPath.isRoot()) {
            return this;
        }
        Path.PathElement element = subPath.getFirstElement();
        if (element.isNamed() || element.getIndex() > values.size()) {
            throw new MissingConfigValueException(
                    "Could not add element on: " + parentPath.add(subPath) +
                            ". Got a list on: " + parentPath + " of size: " + values.size()
            );
        }
        ConfigNode child = getChild(element)
                .map(c -> c.addOrThrow(parentPath.add(element), subPath.removeFirstElement(), value))
                .orElseGet(() -> ConfigNode.of(parentPath, subPath, value));
        return addOrReplaceChild(element, child);
    }

    @Override
    public ConfigNode addOrReplace(Path parentPath, Path subPath, Object value) {
        if (subPath.isRoot()) {
            return new LeafConfigNode(value);
        }
        Path.PathElement element = subPath.getFirstElement();
        if (element.isNamed() || element.getIndex() > values.size()) {
            throw new MissingConfigValueException(
                    "Could not add element on: " + parentPath.add(subPath) +
                            ". Got a list on: " + parentPath + " of size: " + values.size()
            );
        }
        ConfigNode child = getChild(element)
                .map(c -> c.addOrReplace(parentPath.add(element), subPath.removeFirstElement(), value))
                .orElseGet(() -> ConfigNode.of(parentPath, subPath, value));
        return addOrReplaceChild(element, child);
    }

    private ListConfigNode addOrReplaceChild(Path.PathElement element, ConfigNode node) {
        int index = element.getIndex();
        List<ConfigNode> children = new ArrayList<>(this.values);
        children.add(index, node);
        return new ListConfigNode(children);
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
        return new ListConfigNode(mapped);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListConfigNode that = (ListConfigNode) o;
        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
