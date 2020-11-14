package com.coditory.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.coditory.config.ConfigNodeCreator.configNode;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

class MapConfigNode implements ConfigNode {
    private static final MapConfigNode EMPTY_ROOT = new MapConfigNode(Map.of());

    static MapConfigNode emptyRoot() {
        return EMPTY_ROOT;
    }

    private final Map<String, ConfigNode> values;

    MapConfigNode(Map<String, ConfigNode> values) {
        this.values = requireNonNull(values);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public List<Entry<Path, Object>> entries() {
        return values.entrySet().stream()
                .flatMap(entry -> entry.getValue()
                        .entries().stream()
                        .map(subEntry -> concatKeys(entry.getKey(), subEntry))
                )
                .collect(toList());
    }

    private Entry<Path, Object> concatKeys(String childKey, Entry<Path, Object> subEntry) {
        Path key = Path.single(childKey)
                .add(subEntry.getKey());
        return Map.entry(key, subEntry.getValue());
    }

    @Override
    public Optional<ConfigNode> getOptionalNode(Path subPath) {
        if (subPath.isRoot()) {
            return Optional.of(this);
        }
        return getChild(subPath.getFirstElement())
                .flatMap(node -> node.getOptionalNode(subPath.removeFirstElement()));
    }

    @Override
    public Map<String, Object> unwrap() {
        if (values.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(values.size());
        values.forEach((key, value) -> result.put(key, value.unwrap()));
        return unmodifiableMap(result);
    }

    @Override
    public MapConfigNode mapLeaves(Path parentPath, ConfigValueMapper mapper) {
        HashMap<String, ConfigNode> result = new HashMap<>(values.size());
        boolean childModified = false;
        for (Entry<String, ConfigNode> entry : values.entrySet()) {
            Path path = parentPath.add(entry.getKey());
            ConfigNode mapped = entry.getValue().mapLeaves(path, mapper);
            result.put(entry.getKey(), mapped);
            childModified = childModified || !Objects.equals(mapped, entry.getValue());
        }
        return childModified
                ? new MapConfigNode(result)
                : this;
    }

    @Override
    public boolean anyLeaf(Predicate<Object> predicate) {
        return values.values().stream()
                .anyMatch(value -> value.anyLeaf(predicate));
    }

    @Override
    public MapConfigNode addIfMissing(Path parentPath, Path subPath, Object value) {
        if (subPath.isRoot()) {
            return this;
        }
        Path.PathElement element = subPath.getFirstElement();
        if (element.isIndexed()) {
            return this;
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
        if (element.isIndexed()) {
            return configNode(subPath, value);
        }
        ConfigNode child = getChild(element)
                .map(c -> c.addOrReplace(parentPath.add(element), subPath.removeFirstElement(), value))
                .orElseGet(() -> configNode(subPath.removeFirstElement(), value));
        return addOrReplaceChild(element, child);
    }

    @Override
    public MapConfigNode remove(Path parentPath, Path subPath) {
        if (subPath.isRoot() || !subPath.getFirstElement().isNamed()) {
            return this;
        }
        Path.PathElement element = subPath.getFirstElement();
        String name = element.getName();
        if (!values.containsKey(name)) {
            return this;
        }
        Map<String, ConfigNode> result = new HashMap<>(values);
        result.remove(name);
        if (subPath.length() > 1) {
            ConfigNode mappedChild = values.get(name)
                    .remove(parentPath.add(element), subPath.removeFirstElement());
            result.put(name, mappedChild);
        }
        return new MapConfigNode(result);
    }

    @Override
    public MapConfigNode withDefaults(ConfigNode other) {
        if (!(other instanceof MapConfigNode)) {
            return this;
        }
        MapConfigNode otherMapNode = (MapConfigNode) other;
        Map<String, ConfigNode> result = new HashMap<>();
        for (Entry<String, ConfigNode> entry : this.values.entrySet()) {
            ConfigNode otherChildNode = otherMapNode.values.get(entry.getKey());
            ConfigNode mergedChildNode = entry.getValue().withDefaults(otherChildNode);
            result.put(entry.getKey(), mergedChildNode);
        }
        for (Entry<String, ConfigNode> entry : otherMapNode.values.entrySet()) {
            if (!result.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return new MapConfigNode(result);
    }

    private MapConfigNode addOrReplaceChild(Path.PathElement element, ConfigNode node) {
        ConfigNode current = this.values.get(element.getName());
        if (Objects.equals(current, node)) {
            return this;
        }
        HashMap<String, ConfigNode> children = new HashMap<>(this.values);
        children.put(element.getName(), node);
        return new MapConfigNode(children);
    }

    private Optional<ConfigNode> getChild(Path.PathElement element) {
        if (!element.isNamed()) {
            return Optional.empty();
        }
        ConfigNode child = values.get(element.getName());
        return Optional.ofNullable(child);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapConfigNode that = (MapConfigNode) o;
        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
