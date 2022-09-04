package com.coditory.quark.config.format

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigFactory
import com.coditory.quark.config.ConfigFormatter
import spock.lang.Specification

class ConfigPropertiesFormatSpec extends Specification {
    Config config = Config.of([
            a: [
                    b: "B",
                    c: [[d: "D"], "C1"]
            ],
            e: "E",
            f: ["F0", "F1"]
    ])

    String properties = """
    |a.b=B
    |a.c[0].d=D
    |a.c[1]=C1
    |e=E
    |f[0]=F0
    |f[1]=F1
    """.stripMargin().trim() + "\n"

    def "should serialize config to properties"() {
        when:
            String result = ConfigFormatter.toProperties(config)
        then:
            result == properties
    }

    def "should deserialize config from properties"() {
        when:
            Config result = ConfigFactory.parseProperties(properties)
        then:
            result.toMap() == config.toMap()
    }

    def "should hide secrets"() {
        when:
            String result = ConfigFormatter.toProperties(Config.of([secret: "abc"]))
        then:
            result == """secret=***\n"""
    }

    def "should not hide secrets"() {
        when:
            String result = ConfigFormatter.toPropertiesWithExposedSecrets(Config.of([secret: "abc"]))
        then:
            result == """secret=abc\n"""
    }

    def "should deserialize list"() {
        when:
            Config result = ConfigFactory.parseProperties(properties)
        then:
            result.getStringList("f") == ["F0", "F1"]
    }
}
