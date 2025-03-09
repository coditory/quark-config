package com.coditory.quark.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

class Path {
    private static final Path ROOT = new Path(List.of());

    public static Path parseAbsolute(String input) {
        Path path = Path.parse(input);
        if (path.isRoot() || !path.getFirstElement().isNamed()) {
            throw new InvalidConfigPathException(
                    "Expected non empty absolute path to a named element. " +
                            "Example: a.b. Got: " + path);
        }
        return path;
    }

    static Path root() {
        return ROOT;
    }

    static Path single(int index) {
        return of(List.of(new IndexedPathElement(index)));
    }

    static Path single(String name) {
        return of(List.of(new NamedPathElement(name)));
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
        if (path.isEmpty()) return Path.root();
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
        return Arrays.stream(chunk.substring(1, chunk.length() - 1).split("]\\["))
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

    boolean isRoot() {
        return elements.isEmpty();
    }

    PathElement getElement(int index) {
        return elements.get(index);
    }

    Path withChild(String name) {
        List<PathElement> chunks = new ArrayList<>(this.elements);
        chunks.add(new NamedPathElement(name));
        return Path.of(chunks);
    }

    Path withIndexedChild(int index) {
        List<PathElement> chunks = new ArrayList<>(this.elements);
        chunks.add(new IndexedPathElement(index));
        return Path.of(chunks);
    }

    int length() {
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

    Path subPath(int index) {
        return Path.of(elements.subList(0, index + 1));
    }

    PathElement getFirstElement() {
        return elements.getFirst();
    }

    List<String> getPropertyNames() {
        return elements.stream()
                .map(PathElement::name)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    Path removeFirstElement() {
        return Path.of(elements.subList(1, length()));
    }

    Path removePrefix(Path other) {
        if (!this.startsWith(other)) {
            throw new ConfigException("Could not remove path prefix. Paths do not match. " +
                    "Original: " + this + ". Prefix: " + other);
        }
        if (this.equals(other)) {
            return Path.root();
        }
        return Path.of(elements.subList(other.length(), length()));
    }

    Path removeLastElement() {
        return Path.of(elements.subList(0, length() - 1));
    }

    boolean startsWith(Path other) {
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

    Path add(String element) {
        return add(new NamedPathElement(element));
    }

    Path add(int index) {
        return add(new IndexedPathElement(index));
    }

    Path add(Path subPath) {
        List<PathElement> elements = new ArrayList<>();
        elements.addAll(this.elements);
        elements.addAll(subPath.elements);
        return Path.of(elements);
    }

    Path add(PathElement element) {
        List<PathElement> elements = new ArrayList<>(this.elements);
        elements.add(element);
        return Path.of(elements);
    }

    PathElement getLastElement() {
        return elements.get(length() - 1);
    }

    interface PathElement {
        void append(StringBuilder builder);

        default <T> T map(PathElementMapper<T> mapper) {
            if (isNamed()) {
                return mapper.mapName(name());
            }
            if (isIndexed()) {
                return mapper.mapIndex(getIndex());
            }
            throw new IllegalArgumentException("Unrecognized path element: " + this);
        }

        Integer getIndex();

        String name();

        default boolean isNamed() {
            return name() != null;
        }

        default boolean isIndexed() {
            return getIndex() != null;
        }
    }

    interface PathElementMapper<T> {
        T mapName(String name);

        T mapIndex(int index);
    }

    record NamedPathElement(String name) implements PathElement {
        NamedPathElement(String name) {
            if (name == null || name.isBlank()) {
                throw new InvalidConfigPathException("Got blank path element");
            }
            this.name = expectNonBlank(name, "name");
        }

        @Override
        public Integer getIndex() {
            return null;
        }

        @Override
        public void append(StringBuilder builder) {
            if (!builder.isEmpty()) {
                builder.append(".");
            }
            builder.append(name);
        }
    }

    private record IndexedPathElement(int index) implements PathElement {
        private IndexedPathElement {
            if (index < 0) {
                throw new InvalidConfigPathException("Expected non negative index in path. Got: " + index);
            }
        }

        @Override
        public String name() {
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
    }
}
