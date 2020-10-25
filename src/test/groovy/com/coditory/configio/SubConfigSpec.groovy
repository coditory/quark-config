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
            config.getSubConfig("a").toMap() == [
                    b: [c: "ABC"],
                    d: "AD"
            ]
        and:
            config.getSubConfig("a.b").toMap() == [c: "ABC"]
    }

    def "should return sub config from nested values with a list"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[1]", "AB1")
                    .build()
        expect:
            config.getSubConfig("a").toMap() == [
                    b: [[c: "AB0C"], "AB1"]
            ]
        and:
            config.getSubConfig("a.b[0]").toMap() == [c: "AB0C"]
    }

    def "should return empty sub config"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[1]", "AB1")
                    .build()
        expect:
            config.getSubConfigOrEmpty("a.b[2]").isEmpty()
        and:
            config.getSubConfigOrEmpty("b").isEmpty()
    }

    def "should throw error on empty sub config"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[1]", "AB1")
                    .build()

        when:
            config.getSubConfig("a.b[2]")
        then:
            MissingConfigValueException e = thrown(MissingConfigValueException)
            e.message == "Missing config value for path: a.b[2]"

        when:
            config.getSubConfig("b").isEmpty()
        then:
            thrown(MissingConfigValueException)
    }
}
