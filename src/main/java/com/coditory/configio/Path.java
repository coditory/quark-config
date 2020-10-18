package com.coditory.configio;

import com.coditory.configio.api.ConfigioException;
import com.coditory.configio.api.InvalidConfigPathException;

import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.coditory.configio.Preconditions.expectNonEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.toList;

class Path {
    private static final Path ROOT = new Path(List.of());

    public static Path root() {
        return ROOT;
    }

    private static Path of(List<PathElement> chunks) {
        return chunks.isEmpty()
                ? ROOT
                : new Path(chunks);
    }

    static Path parse(String path) {
        try {
            return parseOrThrow(path);
        } catch (RuntimeException e) {
            throw new InvalidConfigPathException("Invalid path: " + path);
        }
    }

    private static Path parseOrThrow(String path) {
        String[] chunks = path.split("\\.");
        List<PathElement> result = new ArrayList<>(chunks.length);
        for (String chunk : chunks) {
            int index = chunk.indexOf('[');
            if (index == 0) {
                matchIndexes(path, chunk)
                        .forEach(v -> result.add(new IndexedPathElement(v)));
            } else if (index > 0) {
                String name = chunk.substring(0, index);
                result.add(new NamedPathElement(name));
                matchIndexes(path, chunk.substring(index))
                        .forEach(v -> result.add(new IndexedPathElement(v)));
            } else {
                result.add(new NamedPathElement(chunk));
            }
        }
        return Path.of(result);
    }

    private static List<Integer> matchIndexes(String fullPath, String chunk) {
        if (chunk.length() < 3 || !chunk.startsWith("[") || !chunk.endsWith("]")) {
            throw new IllegalArgumentException("Could not parse path: " + fullPath +
                    ". Problematic chunk: " + chunk);
        }
        return Arrays.stream(chunk.substring(1, chunk.length() - 1).split("\\]\\["))
                .map(index -> parseIndex(fullPath, index))
                .collect(toList());
    }

    private static Integer parseIndex(String fullPath, String index) {
        try {
            return Integer.parseInt(index);
        } catch (NumberFormatException e) {
            throw new InvalidConfigPathException("Could not parse index value from path: " + fullPath);
        }
    }

    private final String path;
    private final List<PathElement> elements;

    private Path(List<PathElement> elements) {
        this.elements = List.copyOf(elements);
        StringBuilder builder = new StringBuilder();
        elements.forEach(c -> c.append(builder));
        this.path = builder.toString();
    }

    public boolean isRoot() {
        return elements.isEmpty();
    }

    public PathElement getElement(int index) {
        return elements.get(index);
    }

    public Path withChild(String name) {
        List<PathElement> chunks = new ArrayList<>(this.elements);
        chunks.add(new NamedPathElement(name));
        return Path.of(chunks);
    }

    public Path withIndexedChild(int index) {
        List<PathElement> chunks = new ArrayList<>(this.elements);
        chunks.add(new IndexedPathElement(index));
        return Path.of(chunks);
    }

    public int length() {
        return elements.size();
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path1 = (Path) o;
        return Objects.equals(path, path1.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public Path subPath(int index) {
        return Path.of(elements.subList(0, index + 1));
    }

    public PathElement getFirstElement() {
        return elements.get(0);
    }

    public List<String> getPropertyNames() {
        return elements.stream()
                .map(PathElement::getName)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public Path removeFirstElement() {
        return Path.of(elements.subList(1, length()));
    }

    public Path removePrefix(Path other) {
        if (!this.startsWith(other)) {
            throw new ConfigioException("Could not remove path prefix. Paths do not match. " +
                    "Original: " + this + ". Prefix: " + other);
        }
        if (this.equals(other)) {
            return Path.root();
        }
        return Path.of(elements.subList(other.length(), length()));
    }

    public Path removeLastElement() {
        return Path.of(elements.subList(0, length() - 1));
    }

    public boolean startsWith(Path other) {
        requireNonNull(other);
        if (this.length() < other.length()) {
            return false;
        }
        if (this.equals(other)) {
            return true;
        }
        for (int i = 0; i < other.length(); ++i) {
            if (!getElement(i).equals(other.getElement(i))) {
                return false;
            }
        }
        return true;
    }

    public Path add(Path subPath) {
        List<PathElement> elements = new ArrayList<>();
        elements.addAll(this.elements);
        elements.addAll(subPath.elements);
        return Path.of(elements);
    }

    public Path add(PathElement element) {
        List<PathElement> elements = new ArrayList<>(this.elements);
        elements.add(element);
        return Path.of(elements);
    }

    public PathElement getLastElement() {
        return elements.get(length() - 1);
    }

    interface PathElement {
        void append(StringBuilder builder);

        default <T> T map(PathElementMapper<T> mapper) {
            if (isNamed()) {
                return mapper.mapName(getName());
            }
            if (isIndexed()) {
                return mapper.mapIndex(getIndex());
            }
            throw new IllegalArgumentException("Unrecognized path element: " + this);
        }

        Integer getIndex();

        String getName();

        default boolean isNamed() {
            return getName() != null;
        }

        default boolean isIndexed() {
            return getIndex() != null;
        }
    }

    interface PathElementMapper<T> {
        T mapName(String name);
        T mapIndex(int index);
    }

    static class NamedPathElement implements PathElement {
        private final String name;

        public NamedPathElement(String name) {
            if (name == null || name.isBlank()) {
                throw new InvalidConfigPathException("Got blank path element");
            }
            this.name = expectNonEmpty(name, "name");
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getIndex() {
            return null;
        }

        @Override
        public void append(StringBuilder builder) {
            if (builder.length() > 0) {
                builder.append(".");
            }
            builder.append(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NamedPathElement that = (NamedPathElement) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    private static class IndexedPathElement implements PathElement {
        private final int index;

        public IndexedPathElement(int index) {
            if (index < 0) {
                throw new InvalidConfigPathException("Expected non negative index in path. Got: " + index);
            }
            this.index = index;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Integer getIndex() {
            return index;
        }

        @Override
        public void append(StringBuilder builder) {
            builder
                    .append("[")
                    .append(index)
                    .append("]");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IndexedPathElement that = (IndexedPathElement) o;
            return index == that.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(index);
        }
    }
}
