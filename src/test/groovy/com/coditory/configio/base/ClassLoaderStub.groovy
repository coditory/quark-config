package com.coditory.configio.base

import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

class ClassLoaderStub extends ClassLoader {
    private final Map<String, URL> overrides = new ConcurrentHashMap<>()

    void add(String name, URL url) {
        overrides.put(name, url)
    }

    void add(String name, File file) {
        try {
            add(name, file.toURI().toURL())
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid file", e)
        }
    }

    void add(String name, String filePath) {
        add(name, Path.of(filePath).toFile())
    }

    public <T> T setupInThreadContext(Supplier<T> supplier) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader()
        Thread.currentThread().setContextClassLoader(this)
        try {
            return supplier.get()
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader)
        }
    }

    @Override
    URL getResource(String name) {
        URL url = overrides.get(name);
        return url != null
                ? url
                : super.getResource(name)
    }
}
