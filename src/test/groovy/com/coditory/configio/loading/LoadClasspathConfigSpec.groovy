package com.coditory.configio.loading

import com.coditory.configio.Config
import com.coditory.configio.ConfigLoader
import com.coditory.configio.api.ConfigioException
import com.coditory.configio.api.ConfigioParsingException
import com.coditory.configio.base.UsesFiles
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.configio.base.ConfigFormatsSamples.*

class LoadClasspathConfigSpec extends Specification implements UsesFiles {
    @Unroll
    def "should load #extension config from system file"() {
        given:
            String content = sampleConfigPerExt(extension)
            String fileName = "test-loading.$extension"
            writeClasspathFile(fileName, content)
        when:
            Config config = loadFromClasspath(fileName)
        then:
            config.toMap() == sampleConfigMapPerExt(extension)
        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }

    @Unroll
    def "should throw error when loading invalid #extension config from system file"() {
        given:
            String content = sampleInvalidConfigPerExt(extension)
            String fileName = "test-invalid.$extension"
            writeClasspathFile(fileName, content)
        when:
            loadFromClasspath(fileName)
        then:
            ConfigioParsingException e = thrown(ConfigioParsingException)
            e.message.startsWith("Could not parse configuration from classpath file:")
        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }

    @Unroll
    def "should load #extension config from system file without passing extension"() {
        given:
            String content = sampleConfigPerExt(extension)
            String configName = "test-without-extension"
            writeClasspathFile("$configName.$extension", content)
        when:
            Config config = loadFromClasspathInAnyFormat(configName)
        then:
            config.toMap() == sampleConfigMapPerExt(extension)
        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }

    @Unroll
    def "should load #extension before others: #others"() {
        given:
            String configName = "test-order"
            writeClasspathFile("$configName.$extension", content)
        and:
            others.each {
                writeClasspathFile("$configName.$it", sampleConfigPerExt(it))
            }
        when:
            Config config = loadFromClasspathInAnyFormat(configName)
        then:
            config.getString("value") == extension
        where:
            others                         | extension | content
            ["yaml", "json", "properties"] | "yml"     | "value: yml"
            ["json", "properties"]         | "yaml"    | "value: yaml"
            ["properties"]                 | "json"    | "{ \"value\": \"json\" }"
    }

    def "should throw error when loading non-existent file in any format"() {
        when:
            loadFromClasspathInAnyFormat("test-non-existent")
        then:
            ConfigioException e = thrown(ConfigioException)
            e.message.startsWith("Could not load configuration. Files were not found on CLASSPATH: [")
    }

    def "should throw error when loading invalid file extension"() {
        given:
            String fileName = "test-invalid-ext.abc"
            writeClasspathFile(fileName, "content")
        when:
            loadFromClasspath(fileName)
        then:
            ConfigioException e = thrown(ConfigioException)
            e.getMessage().stripMargin("Unrecognized config format for file path:")
    }

    @Unroll
    def "should throw error when loading non-existent file #extension"() {
        when:
            ConfigLoader.loadFromClasspath("test-non-existent.$extension")
        then:
            ConfigioException e = thrown(ConfigioException)
            e.message.startsWith("Could not load configuration. File was not found on CLASSPATH: ")

        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }

    private Config loadFromClasspath(String path) {
        return stubClassLoader {
            ConfigLoader.loadFromClasspath(path)
        }
    }

    private Config loadFromClasspathInAnyFormat(String path) {
        return stubClassLoader {
            ConfigLoader.loadFromClasspathInAnyFormat(path)
        }
    }
}
