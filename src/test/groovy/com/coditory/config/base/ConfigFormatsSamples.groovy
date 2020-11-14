package com.coditory.config.base

import groovy.transform.CompileStatic

@CompileStatic
class ConfigFormatsSamples {
    static final String SAMPLE_YML_CONFIG = """
    |server.port: 8080
    |# some comment
    |other: other
    """.stripMargin().trim()

    static final String SAMPLE_INVALID_YML_CONFIG = """
    |a : {hello}
    """.stripMargin().trim()

    static final String SAMPLE_JSON_CONFIG = """
    |{
    |  "server": { "port": 8080 },
    |  "other": "other"
    |}
    """.stripMargin().trim()

    static final String SAMPLE_INVALID_JSON_CONFIG = """
    |{a}
    """.stripMargin().trim()

    static final String SAMPLE_PROPERTIES_CONFIG = """
    |server.port=8080
    |# some comment
    |other=other
    """.stripMargin().trim()

    static final String SAMPLE_INVALID_PROPERTIES_CONFIG = """
    |==
    """.stripMargin().trim()

    private static final Map<String, ?> SAMPLE_CONFIG_MAP = [
            server: [port: 8080],
            other : "other"
    ].asImmutable()

    private static final Map<String, ?> SAMPLE_CONFIG_MAP_FOR_PROPERTIES = [
            server: [port: "8080"],
            other : "other"
    ].asImmutable()

    static Map<String, ?> sampleConfigMapPerExt(String extension) {
        return extension == "properties"
                ? SAMPLE_CONFIG_MAP_FOR_PROPERTIES
                : SAMPLE_CONFIG_MAP
    }

    private static final Map<String, String> SAMPLE_CONFIGS_PER_EXT = [
            yml       : SAMPLE_YML_CONFIG,
            yaml      : SAMPLE_YML_CONFIG,
            json      : SAMPLE_JSON_CONFIG,
            properties: SAMPLE_PROPERTIES_CONFIG
    ].asImmutable()

    static String sampleConfigPerExt(String extension) {
        String content = SAMPLE_CONFIGS_PER_EXT.get(extension)
        if (content == null) {
            throw new IllegalArgumentException("Sample config content not found for extension: " + extension)
        }
        return content
    }

    private static final Map<String, String> SAMPLE_INVALID_CONFIGS_PER_EXT = [
            yml       : SAMPLE_INVALID_YML_CONFIG,
            yaml      : SAMPLE_INVALID_YML_CONFIG,
            json      : SAMPLE_INVALID_JSON_CONFIG,
            properties: SAMPLE_INVALID_PROPERTIES_CONFIG
    ].asImmutable()

    static String sampleInvalidConfigPerExt(String extension) {
        String content = SAMPLE_INVALID_CONFIGS_PER_EXT.get(extension)
        if (content == null) {
            throw new IllegalArgumentException("Sample invalid config content not found for extension: " + extension)
        }
        return content
    }

    static final Set<String> SAMPLE_CONFIGS_EXTENSIONS = [
            "yml",
            "yaml",
            "json",
            "properties"
    ].toSet().asImmutable()
}
