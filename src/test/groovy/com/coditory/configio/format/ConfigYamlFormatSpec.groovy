package com.coditory.configio.format

import com.coditory.configio.Config
import com.coditory.configio.ConfigFactory
import com.coditory.configio.ConfigFormatter
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
    |  - {d: D}
    |  - C1
    |e: E
    |f: [F0, F1]
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
}
