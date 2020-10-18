package com.coditory.configio

import com.coditory.configio.api.MissingConfigValueException
import spock.lang.Specification

class ValueRetrievalSpec extends Specification {
    def "should retrieve nested values by path"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e", "E")
                    .build()
        expect:
            config.getString("a.b.c") == "ABC"
        and:
            config.getString("a.d") == "AD"
        and:
            config.getString("e") == "E"
    }

    def "should return retrieve nested value from inserted map"() {
        given:
            Config config = Config.builder()
                    .withValue("a", [b: [c: "ABC"], d: "AD"])
                    .build()
        expect:
            config.getString("a.b.c") == "ABC"
        and:
            config.getString("a.d") == "AD"
    }

    def "should not retrieve missing value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e", "E")
                    .build()
        when:
            config.getString("a.e")
        then:
            MissingConfigValueException e = thrown(MissingConfigValueException)
            e.message == "Missing config value for path: a.e"
        and:
            config.getStringOrNull("a.e") == null
        and:
            config.getStringOrDefault("a.e", "X") == "X"
    }

    def "should return retrieve nested value in a list"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[1]", "AB1")
                    .build()
        expect:
            config.getString("a.b[0].c") == "AB0C"
        and:
            config.getString("a.b[1]") == "AB1"
        and:
            config.getStringOrNull("a.b[0].d") == null
        and:
            config.getStringOrNull("a.b[5].d") == null
        and:
            config.getStringOrNull("a.b.d") == null
    }

    def "should return retrieve nested value from inserted list"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", [[c: "AB0C"], "AB1"])
                    .build()
        expect:
            config.getString("a.b[0].c") == "AB0C"
        and:
            config.getString("a.b[1]") == "AB1"
    }
}
