package com.coditory.configio;

import com.coditory.configio.api.MissingConfigValueException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

class MapConfigNode implements ConfigNode {
    private static final MapConfigNode EMPTY_ROOT = new MapConfigNode(Path.root(), Map.of());

    static MapConfigNode emptyRoot() {
        return EMPTY_ROOT;
    }

    private final Path path;
    private final Map<String, ConfigNode> values;

    MapConfigNode(Path path, Map<String, ConfigNode> values) {
        this.path = requireNonNull(path);
        this.values = requireNonNull(values);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
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
    public Object getOrThrow(Path subPath) {
        if (subPath.isRoot()) {
            return unwrap();
        }
        return getChild(subPath.getFirstElement())
                .orElseThrow(() -> {
                    String message = "Could not get element for path: " + path +
                            ". Got map on path: " + this.path + " with keys: " + values.keySet();
                    return new MissingConfigValueException(message);
                });
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public Map<String, Object> unwrap() {
        if (values.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> result = new HashMap<>(values.size());
        values.forEach((key, value) -> result.put(key, value.unwrap()));
        return Map.copyOf(result);
    }

    @Override
    public ConfigNode mapLeaves(Function<Object, Object> mapper) {
        Map<String, ConfigNode> mapped = values.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().mapLeaves(mapper)));
        return new MapConfigNode(path, mapped);
    }

    @Override
    public MapConfigNode addIfMissing(Path subPath, Object value) {
        if (subPath.isRoot()) {
            return this;
        }
        ConfigNode child = getChild(subPath.getFirstElement())
                .map(c -> c.addIfMissing(subPath.removeFirstElement(), value))
                .orElseGet(() -> ConfigNode.of(path, subPath, value));
        return this.addOrReplaceChild(child);
    }

    @Override
    public MapConfigNode addOrThrow(Path subPath, Object value) {
        if (subPath.isRoot()) {
            return this;
        }
        return getChild(subPath.getFirstElement())
                .map(c -> c.addOrThrow(subPath.removeFirstElement(), value))
                .map(c -> this.addOrReplaceChild(c))
                .orElseThrow(() -> {
                    String message = "Could not add element on path: " + path.add(subPath) +
                            ". Got map on path: " + this.path + " with keys: " + values.keySet();
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
                .map(c -> this.addOrReplaceChild(c))
                .orElse(this);
    }

    private MapConfigNode addOrReplaceChild(ConfigNode node) {
        Path.PathElement element = node.path().getLastElement();
        ConfigNode current = this.values.get(element.getName());
        if (Objects.equals(current, node)) {
            return this;
        }
        HashMap<String, ConfigNode> children = new HashMap<>(this.values);
        children.put(element.getName(), node);
        return new MapConfigNode(path, children);
    }

    private Optional<ConfigNode> getChild(Path.PathElement element) {
        if (!element.isNamed()) {
            return Optional.empty();
        }
        ConfigNode child = values.get(element.getName());
        return Optional.ofNullable(child);
    }
}
