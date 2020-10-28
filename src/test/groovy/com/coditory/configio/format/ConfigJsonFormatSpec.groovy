package com.coditory.configio.format

import com.coditory.configio.Config
import com.coditory.configio.ConfigFactory
import com.coditory.configio.ConfigFormatter
import spock.lang.Specification

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals

class ConfigJsonFormatSpec extends Specification {
    Config config = Config.of([
            a: [
                    b: "B",
                    c: [[d: "D"], "C1"]
            ],
            e: "E",
            f: ["F0", "F1"]
    ])

    String json = """
    |{
    |  "a": {
    |    "b": "B",
    |    "c": [{ "d": "D" }, "C1"]
    |  },
    |  "e": "E",
    |  "f": ["F0", "F1"]
    |}
    """.stripMargin().trim()

    def "should serialize config to json"() {
        when:
            String result = ConfigFormatter.toJson(config)
        then:
            assertEquals(json, result, true)
    }

    def "should deserialize config from json"() {
        when:
            Config result = ConfigFactory.parseJson(json)
        then:
            result.toMap() == config.toMap()
    }

    def "should hide secrets"() {
        when:
            String result = ConfigFormatter.toJson(Config.of([secret: "abc"]))
        then:
            assertEquals(result, """{ "secret": "***" }""", true)
    }

    def "should not hide secrets"() {
        when:
            String result = ConfigFormatter.toJsonWithSecretsExposed(Config.of([secret: "abc"]))
        then:
            assertEquals(result, """{ "secret": "abc" }""", true)
    }
}
