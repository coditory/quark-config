package com.coditory.config

import spock.lang.Specification

class ConfigComparisonSpec extends Specification {
    def "empty configs should be equal"() {
        expect:
            Config.empty() == Config.empty()
        and:
            Config.empty() == Config.of(Map.of())
    }

    def "empty configs with different valueParsers should differ"() {
        given:
            Config emptyConfig = Config.empty()
            Config otherEmptyConfig = Config.builder()
                    .withValueParsers(List.of())
                    .build()
        expect:
            emptyConfig != otherEmptyConfig
    }

    def "configs with the same values and parsers should be equal"() {
        given:
            Config config = Config.of([a: "A"])
            Config other = Config.of([a: "A"])
        expect:
            config == other
    }

    def "configs with the same values and different parsers should differ"() {
        given:
            Config config = Config.builder()
                    .withValue("a", "A")
                    .build()
            Config other = Config.builder()
                    .withValue("a", "A")
                    .withValueParsers(List.of())
                    .build()
        expect:
            config != other
    }

    def "configs with different values and same parsers should differ"() {
        given:
            Config config = Config.builder()
                    .withValue("a", "A")
                    .build()
            Config other = Config.builder()
                    .withValue("a", "B")
                    .build()
        expect:
            config != other
    }

    def "should deeply compare config values"() {
        given:
            Config config = Config.of([
                    a: [
                            b: [c: "ABC"],
                            d: "AD"
                    ],
                    e: "E"
            ])
        expect:
            config != Config.of([
                    a: [
                            b: [c: "ABCX"],
                            d: "AD"
                    ],
                    e: "E"
            ])
        and:
            config == Config.of([
                    a: [
                            b: [c: "ABC"],
                            d: "AD"
                    ],
                    e: "E"
            ])
    }

    def "should deeply compare list config values"() {
        given:
            Config config = Config.of([
                    a: [
                            [c: "ABC"],
                            "AD"
                    ],
                    e: "E"
            ])
        expect:
            config != Config.of([
                    a: [
                            [c: "ABCX"],
                            "AD"
                    ],
                    e: "E"
            ])
        and:
            config == Config.of([
                    a: [
                            [c: "ABC"],
                            "AD"
                    ],
                    e: "E"
            ])
    }
}
