package com.coditory.quark.config.format

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigFactory
import com.coditory.quark.config.ConfigFormatter
import spock.lang.Specification

class ConfigYamlFormatSpec extends Specification {
    Config config = Config.of([
            a: [
                    b: "B",
                    c: [[d: "D"], "C1"]
            ],
            e: "E",
            f: ["F0", "F1"]
    ])

    String yaml = """
    |a:
    |  b: B
    |  c:
    |  - d: D
    |  - C1
    |e: E
    |f:
    |- F0
    |- F1
    """.stripMargin().trim() + "\n"

    def "should serialize config to yaml"() {
        when:
            String result = ConfigFormatter.toYaml(config)
        then:
            result == yaml
    }

    def "should deserialize config from yaml"() {
        when:
            Config result = ConfigFactory.parseYaml(yaml)
        then:
            result.toMap() == config.toMap()
    }

    def "should hide secrets"() {
        when:
            String result = ConfigFormatter.toYaml(Config.of([secret: "abc"]))
        then:
            result == """secret: '***'\n"""
    }

    def "should not hide secrets"() {
        when:
            String result = ConfigFormatter.toYamlWithExposedSecrets(Config.of([secret: "abc"]))
        then:
            result == """secret: abc\n"""
    }
}
