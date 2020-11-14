package com.coditory.quark.config.loading

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigFactory
import com.coditory.quark.config.ConfigException
import com.coditory.quark.config.ConfigLoadException
import com.coditory.quark.config.ConfigParseException
import com.coditory.quark.config.base.UsesFiles
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.config.base.ConfigFormatsSamples.sampleConfigMapPerExt
import static com.coditory.quark.config.base.ConfigFormatsSamples.sampleConfigPerExt
import static com.coditory.quark.config.base.ConfigFormatsSamples.SAMPLE_CONFIGS_EXTENSIONS
import static com.coditory.quark.config.base.ConfigFormatsSamples.sampleInvalidConfigPerExt

class LoadFileSystemConfigSpec extends Specification implements UsesFiles {
    @Unroll
    def "should load #extension config from system file"() {
        given:
            String content = sampleConfigPerExt(extension)
            File file = writeFile("test-loading.$extension", content)
        when:
            Config config = ConfigFactory.loadFromFileSystem(file.getPath())
        then:
            config.toMap() == sampleConfigMapPerExt(extension)
        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }

    @Unroll
    def "should throw error when loading invalid #extension config from system file"() {
        given:
            String content = sampleInvalidConfigPerExt(extension)
            File file = writeFile("test-invalid.$extension", content)
        when:
            ConfigFactory.loadFromFileSystem(file.getPath())
        then:
            ConfigParseException e = thrown(ConfigParseException)
            e.message.startsWith("Could not parse configuration from file system:")
        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }

    @Unroll
    def "should load #extension config from system file without passing extension"() {
        given:
            String content = sampleConfigPerExt(extension)
            String configName = "test-without-extension"
            File file = writeFile("$configName.$extension", content)
            String path = file.getPath().replace("$configName.$extension", configName)
        when:
            Config config = ConfigFactory.loadFromFileSystem(path)
        then:
            config.toMap() == sampleConfigMapPerExt(extension)
        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }

    @Unroll
    def "should load #extension before others: #others"() {
        given:
            String configName = "test-order"
            File file = writeFile("$configName.$extension", content)
            String path = file.getPath().replace("$configName.$extension", configName)
        and:
            others.each {
                writeFile("$configName.$it", sampleConfigPerExt(it))
            }
        when:
            Config config = ConfigFactory.loadFromFileSystem(path)
        then:
            config.getString("value") == extension
        where:
            others                         | extension | content
            ["yaml", "json", "properties"] | "yml"     | "value: yml"
            ["json", "properties"]         | "yaml"    | "value: yaml"
            ["properties"]                 | "json"    | "{ \"value\": \"json\" }"
    }

    def "should throw error when loading invalid file extension"() {
        given:
            File file = writeFile("test-invalid-ext.abc", "content")
        when:
            ConfigFactory.loadFromFileSystem(file.getPath())
        then:
            ConfigException e = thrown(ConfigException)
            e.getMessage().stripMargin("Unrecognized config format for file path:")
    }

    def "should throw error when loading non-existent file in any format"() {
        given:
            String path = tempDirectory.toPath()
                    .resolve("test-non-existent")
        when:
            ConfigFactory.loadFromFileSystem(path)
        then:
            ConfigLoadException e = thrown(ConfigLoadException)
            e.message.startsWith("Configuration file not found on file system: ")
    }

    @Unroll
    def "should throw error when loading non-existent file #extension"() {
        given:
            String configName = "test-non-existent"
            String path = tempDirectory.toPath()
                    .resolve("$configName.$extension")

        when:
            ConfigFactory.loadFromFileSystem(path)
        then:
            ConfigLoadException e = thrown(ConfigLoadException)
            e.message.startsWith("Configuration file not found on file system: ")

        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }
}
