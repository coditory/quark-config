package com.coditory.quark.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.coditory.quark.config.ConfigNodeCreator.configNode;
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
    public List<Entry<Path, Object>> entries() {
        List<Entry<Path, Object>> entries = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); ++i) {
            final int index = i;
            values.get(i).entries().stream()
                    .map(subEntry -> concatKeys(index, subEntry))
                    .forEach(entries::add);
        }
        return entries;
    }

    private Entry<Path, Object> concatKeys(int childIndex, Entry<Path, Object> subEntry) {
        Path key = Path.single(childIndex)
                .add(subEntry.getKey());
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
                .orElseGet(() -> configNode(subPath.removeFirstElement(), value));
        return addOrReplaceChild(element, child);
    }

    @Override
    public ConfigNode addOrReplace(Path parentPath, Path subPath, Object value) {
        if (subPath.isRoot()) {
            return configNode(subPath, value);
        }
        Path.PathElement element = subPath.getFirstElement();
        if (element.isNamed()) {
            return configNode(subPath, value);
        }
        if (element.getIndex() > values.size()) {
            throw new MissingConfigValueException(
                    "Could not add element on: " + parentPath.add(subPath) +
                            ". Got a list on: " + parentPath + " of size: " + values.size()
            );
        }
        ConfigNode child = getChild(element)
                .map(c -> c.addOrReplace(parentPath.add(element), subPath.removeFirstElement(), value))
                .orElseGet(() -> configNode(subPath.removeFirstElement(), value));
        return addOrReplaceChild(element, child);
    }

    @Override
    public ListConfigNode withDefaults(ConfigNode other) {
        return this;
    }

    @Override
    public boolean anyLeaf(Predicate<Object> predicate) {
        return values.stream()
                .anyMatch(value -> value.anyLeaf(predicate));
    }

    @Override
    public ListConfigNode remove(Path parentPath, Path subPath, ConfigRemoveOptions options) {
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
                    .remove(parentPath.add(element), subPath.removeFirstElement(), options);
            if (mappedChild != null) {
                result.add(index, mappedChild);
            }
        }
        if (result.isEmpty() && options.isRemoveEmptyLists()) {
            return null;
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
    public ListConfigNode filterLeaves(Path parentPath, ConfigEntryPredicate predicate, ConfigRemoveOptions options) {
        List<ConfigNode> result = new ArrayList<>(values.size());
        boolean childMapped = false;
        for (int i = 0; i < values.size(); ++i) {
            Path path = parentPath.add(i);
            ConfigNode child = values.get(i);
            ConfigNode mapped = child.filterLeaves(path, predicate, options);
            if (mapped != null) {
                result.add(mapped);
            }
            childMapped = childMapped || !Objects.equals(mapped, child);
        }
        if (result.isEmpty() && options.isRemoveEmptyLists()) {
            return null;
        }
        return childMapped
                ? new ListConfigNode(result)
                : this;
    }

    @Override
    public ListConfigNode mapLeaves(Path parentPath, ConfigEntryMapper mapper) {
        List<ConfigNode> result = new ArrayList<>(values.size());
        boolean childMapped = false;
        for (int i = 0; i < values.size(); ++i) {
            Path path = parentPath.add(i);
            ConfigNode child = values.get(i);
            ConfigNode mapped = child.mapLeaves(path, mapper);
            result.add(mapped);
            childMapped = childMapped || !Objects.equals(mapped, child);
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
