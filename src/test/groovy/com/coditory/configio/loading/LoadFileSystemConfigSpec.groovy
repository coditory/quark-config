package com.coditory.configio.loading

import com.coditory.configio.Config
import com.coditory.configio.ConfigLoader
import com.coditory.configio.api.ConfigioException
import com.coditory.configio.api.ConfigioParsingException
import com.coditory.configio.base.UsesFiles
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.configio.base.ConfigFormatsSamples.*

class LoadFileSystemConfigSpec extends Specification implements UsesFiles {
    @Unroll
    def "should load #extension config from system file"() {
        given:
            String content = sampleConfigPerExt(extension)
            File file = writeFile("test-loading.$extension", content)
        when:
            Config config = ConfigLoader.loadFromFileSystem(file.getPath())
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
            ConfigLoader.loadFromFileSystem(file.getPath())
        then:
            ConfigioParsingException e = thrown(ConfigioParsingException)
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
            Config config = ConfigLoader.loadFromSystemFileInAnyFormat(path)
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
            Config config = ConfigLoader.loadFromSystemFileInAnyFormat(path)
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
            ConfigLoader.loadFromFileSystem(file.getPath())
        then:
            ConfigioException e = thrown(ConfigioException)
            e.getMessage().stripMargin("Unrecognized config format for file path:")
    }

    def "should throw error when loading non-existent file in any format"() {
        given:
            String path = tempDirectory.toPath()
                    .resolve("test-non-existent")
        when:
            ConfigLoader.loadFromSystemFileInAnyFormat(path)
        then:
            ConfigioException e = thrown(ConfigioException)
            e.message.startsWith("Could not load configuration. Files were not found on FILE_SYSTEM: [")
    }

    @Unroll
    def "should throw error when loading non-existent file #extension"() {
        given:
            String configName = "test-non-existent"
            String path = tempDirectory.toPath()
                    .resolve("$configName.$extension")

        when:
            ConfigLoader.loadFromFileSystem(path)
        then:
            ConfigioException e = thrown(ConfigioException)
            e.message.startsWith("Could not load configuration. File was not found on FILE_SYSTEM: ")

        where:
            extension << SAMPLE_CONFIGS_EXTENSIONS
    }
}
