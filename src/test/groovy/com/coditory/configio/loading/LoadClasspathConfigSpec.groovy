package com.coditory.configio.loading

import com.coditory.configio.Config
import com.coditory.configio.ConfigLoader
import com.coditory.configio.api.ConfigException
import com.coditory.configio.api.ConfigLoadException
import com.coditory.configio.api.ConfigParseException
import com.coditory.configio.base.UsesFiles
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.configio.base.ConfigFormatsSamples.SAMPLE_CONFIGS_EXTENSIONS
import static com.coditory.configio.base.ConfigFormatsSamples.sampleConfigMapPerExt
import static com.coditory.configio.base.ConfigFormatsSamples.sampleConfigPerExt
import static com.coditory.configio.base.ConfigFormatsSamples.sampleInvalidConfigPerExt

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
            ConfigParseException e = thrown(ConfigParseException)
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
            Config config = loadFromClasspath(configName)
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
            Config config = loadFromClasspath(configName)
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
            loadFromClasspath("test-non-existent")
        then:
            ConfigLoadException e = thrown(ConfigLoadException)
            e.message.startsWith("Configuration file not found on classpath: ")
    }

    def "should throw error when loading invalid file extension"() {
        given:
            String fileName = "test-invalid-ext.abc"
            writeClasspathFile(fileName, "content")
        when:
            loadFromClasspath(fileName)
        then:
            ConfigException e = thrown(ConfigException)
            e.getMessage().stripMargin("Unrecognized config format for file path:")
    }

    @Unroll
    def "should throw error when loading non-existent file #extension"() {
        when:
            ConfigLoader.loadFromClasspath("test-non-existent.$extension")
        then:
            ConfigLoadException e = thrown(ConfigLoadException)
            e.message.startsWith("Configuration file not found on classpath: ")

        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }

    private Config loadFromClasspath(String path) {
        return stubClassLoader {
            ConfigLoader.loadFromClasspath(path)
        }
    }
}
