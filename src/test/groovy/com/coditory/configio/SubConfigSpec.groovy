package com.coditory.configio

import com.coditory.configio.api.MissingConfigValueException
import spock.lang.Specification

class SubConfigSpec extends Specification {
    def "should return sub config from nested values"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e", "E")
                    .build()
        expect:
            config.subConfig("a").toMap() == [
                    b: [c: "ABC"],
                    d: "AD"
            ]
        and:
            config.subConfig("a.b").toMap() == [c: "ABC"]
    }

    def "should return sub config from nested values with a list"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[1]", "AB1")
                    .build()
        expect:
            config.subConfig("a").toMap() == [
                    b: [[c: "AB0C"], "AB1"]
            ]
        and:
            config.subConfig("a.b[0]").toMap() == [c: "AB0C"]
    }

    def "should return empty sub config"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[1]", "AB1")
                    .build()
        expect:
            config.subConfigOrEmpty("a.b[2]").isEmpty()
        and:
            config.subConfigOrEmpty("b").isEmpty()
    }

    def "should throw error on empty sub config"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[1]", "AB1")
                    .build()

        when:
            config.subConfig("a.b[2]").isEmpty()
        then:
            thrown(MissingConfigValueException)

        when:
            config.subConfig("b").isEmpty()
        then:
            thrown(MissingConfigValueException)
    }
}
