package com.coditory.quark.config

import spock.lang.Specification

class ConfigWithDefaultSpec extends Specification {
    def "should add values to an empty config"() {
        given:
            Config config = Config.empty()
        when:
            config = config
                    .withDefault("a.b.c", "ABC")
                    .withDefault("a.d", "AD")
                    .withDefault("e", ["E0", "E1"])
        then:
            config.toMap() == [
                    a: [
                            b: [c: "ABC"],
                            d: "AD"
                    ],
                    e: ["E0", "E1"]
            ]
    }

    def "should not overwrite previously added values"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d.e", "ADE")
                    .withValue("e", ["E0", "E1"])
                    .build()
            Config copy = config
        when:
            config = config
                    .withDefault("a.b.c", "X")
                    .withDefault("a.d", "Y")
                    .withDefault("e[1]", "Z")
        then:
            config.toMap() == copy.toMap()
    }

    def "should add new values to list of values"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", [[c: "C"], "e"])
                    .build()
        when:
            config = config
                    .withDefault("a.b[0].d", "D")
                    .withDefault("a.b[0].e[0][0]", "E00")
                    .withDefault("a.b[2]", "3")
        then:
            config.toMap() == [
                    a: [
                            b: [
                                    [c: "C", d: "D", e: [["E00"]]],
                                    "e",
                                    "3"
                            ]
                    ]
            ]
    }

    def "should not replace list value with nested value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", ["c", "d"])
                    .build()
            Config copy = config
        when:
            config = config
                    .withDefault("a.b.c.d", "D")
        then:
            config.toMap() == copy.toMap()
    }

    def "should not replace nested value with list value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c.d", "d")
                    .build()
            Config copy = config
        when:
            config = config
                    .withDefault("a.b", ["c", "d"])
        then:
            config.toMap() == copy.toMap()
    }

    def "should not replace value with other value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c.d", "d")
                    .withValue("a.b.e", "e")
                    .build()
            Config copy = config
        when:
            config = config
                    .withDefault("a.b.c.d", "X")
                    .withDefault("a.b.e.d", "Y")
        then:
            config.toMap() == copy.toMap()
    }

    def "should fail adding list item with invalid index"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", ["c", "d"])
                    .build()
        when:
            config.withDefault("a.b[3]", "e")
        then:
            thrown(MissingConfigValueException)

        when:
            config.withDefault("a.b[-1]", "e")
        then:
            thrown(InvalidConfigPathException)
    }
}
