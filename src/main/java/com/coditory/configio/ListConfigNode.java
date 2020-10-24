package com.coditory.configio;

import com.coditory.configio.api.MissingConfigValueException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.coditory.configio.ConfigNodeCreator.configNode;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
    public Set<Entry<String, Object>> entries() {
        Set<Entry<String, Object>> entries = new LinkedHashSet<>();
        for (int i = 0; i < values.size(); ++i) {
            final int index = i;
            ConfigNode node = values.get(i);
            values.stream()
                    .flatMap(value -> value.entries().stream())
                    .map(subEntry -> concatKeys(node, index, subEntry))
                    .forEach(entries::add);
        }
        return entries;
    }

    private Entry<String, Object> concatKeys(ConfigNode node, int index, Entry<String, Object> subEntry) {
        String indexString = "[" + index+ "]";
        String key = (node instanceof ListConfigNode)
                ? indexString + subEntry.getKey()
                : indexString + "." + subEntry.getKey();
        return Map.entry(key, subEntry.getValue());
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
        if (element.isNamed()) {
            return this;
        }
        if (element.getIndex() > values.size()) {
            throw new MissingConfigValueException(
                    "Could not add element on: " + parentPath.add(subPath) +
                            ". Got a list on: " + parentPath + " of size: " + values.size()
            );
        }
        ConfigNode child = getChild(element)
                .map(c -> c.addIfMissing(parentPath.add(element), subPath.removeFirstElement(), value))
                .orElseGet(() -> configNode(parentPath.add(element), subPath.removeFirstElement(), value));
        return addOrReplaceChild(element, child);
    }

    @Override
    public ConfigNode addOrReplace(Path parentPath, Path subPath, Object value) {
        if (subPath.isRoot()) {
            return configNode(parentPath, subPath, value);
        }
        Path.PathElement element = subPath.getFirstElement();
        if (element.isNamed()) {
            return configNode(parentPath, subPath, value);
        }
        if (element.getIndex() > values.size()) {
            throw new MissingConfigValueException(
                    "Could not add element on: " + parentPath.add(subPath) +
                            ". Got a list on: " + parentPath + " of size: " + values.size()
            );
        }
        ConfigNode child = getChild(element)
                .map(c -> c.addOrReplace(parentPath.add(element), subPath.removeFirstElement(), value))
                .orElseGet(() -> configNode(parentPath.add(element), subPath.removeLastElement(), value));
        return addOrReplaceChild(element, child);
    }

    @Override
    public ListConfigNode withDefaults(ConfigNode other) {
        return this;
    }

    @Override
    public boolean anyLeaf(Predicate<Object> predicate) {
        return values.stream()
                .anyMatch(predicate);
    }

    @Override
    public ListConfigNode remove(Path parentPath, Path subPath) {
        if (subPath.isRoot() || !subPath.getFirstElement().isIndexed()) {
            return this;
        }
        Path.PathElement element = subPath.getFirstElement();
        int index = element.getIndex();
        if (index >= values.size()) {
            return this;
        }
        List<ConfigNode> result = new ArrayList<>(values);
        result.remove(index);
        if (subPath.length() > 1) {
            ConfigNode mappedChild = values.get(index)
                    .remove(parentPath.add(element), subPath.removeFirstElement());
            result.add(index, mappedChild);
        }
        return new ListConfigNode(result);
    }

    private ListConfigNode addOrReplaceChild(Path.PathElement element, ConfigNode node) {
        int index = element.getIndex();
        List<ConfigNode> children = new ArrayList<>(this.values);
        if (index < children.size()) {
            children.remove(index);
        }
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
    public ListConfigNode mapLeaves(Function<Object, Object> mapper) {
        List<ConfigNode> result = new ArrayList<>(values.size());
        boolean childMapped = false;
        for (ConfigNode child : values) {
            ConfigNode mapped = child.mapLeaves(mapper);
            result.add(mapped);
            childMapped = childMapped || Objects.equals(mapped, child);
        }
        return childMapped
                ? new ListConfigNode(result)
                : this;
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
