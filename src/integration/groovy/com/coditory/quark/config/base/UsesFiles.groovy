package com.coditory.quark.config.base

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Supplier

@CompileStatic
trait UsesFiles {
    File tempDirectory

    ClassLoaderStub classLoader = new ClassLoaderStub()

    void setup() {
        tempDirectory = File.createTempDir("temp", Long.toString(System.nanoTime()))
    }

    void cleanup() {
        tempDirectory.delete()
    }

    public <T> T stubClassLoader(Supplier<T> supplier) {
        return classLoader.setupInThreadContext(supplier)
    }

    File writeClasspathFile(String fileName, String content) {
        File file = writeFile(fileName, content)
        classLoader.add(fileName, file)
        return file
    }

    File writeFile(String fileName, String content) {
        Path path = Paths.get(tempDirectory.path, fileName)
        File file = Files.createFile(path).toFile()
        file.write(content.stripIndent().trim())
        return file
    }
}