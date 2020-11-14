package com.coditory.config


import spock.lang.Specification

class ConfigWithValueSpec extends Specification {
    def "should add values to an empty config"() {
        given:
            Config config = Config.empty()
        when:
            config = config
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e", ["E0", "E1"])
        then:
            config.toMap() == [
                    a: [
                            b: [c: "ABC"],
                            d: "AD"
                    ],
                    e: ["E0", "E1"]
            ]
    }

    def "should overwrite previously added values"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d.e", "ADE")
                    .withValue("e", ["E0", "E1"])
                    .build()
        when:
            config = config
                    .withValue("a.b.c", "X")
                    .withValue("a.d", "Y")
                    .withValue("e[1]", "Z")
        then:
            config.toMap() == [
                    a: [
                            b: [c: "X"],
                            d: "Y"
                    ],
                    e: ["E0", "Z"]
            ]
    }

    def "should add new values to a list values"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", [[c: "ABC"], "d", "e"])
                    .build()
        when:
            config = config
                    .withValue("a.b[0].c", "C")
                    .withValue("a.b[0].d", "D")
                    .withValue("a.b[0].e[0][0]", "E00")
                    .withValue("a.b[1]", [z: "Z"])
                    .withValue("a.b[3]", "3")
        then:
            config.toMap() == [
                    a: [
                            b: [
                                    [c: "C", d: "D", e: [["E00"]]],
                                    [z: "Z"],
                                    "e",
                                    "3"
                            ]
                    ]
            ]
    }

    def "should replace list value with nested value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", ["c", "d"])
                    .build()
        when:
            config = config
                    .withValue("a.b.c.d", "D")
        then:
            config.toMap() == [
                    a: [b: [c: [d: "D"]]]
            ]
    }

    def "should replace nested value with list value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c.d", "d")
                    .build()
        when:
            config = config
                    .withValue("a.b", ["c", "d"])
        then:
            config.toMap() == [
                    a: [b: ["c", "d"]]
            ]
    }

    def "should replace value with other value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "B")
                    .withValue("a.c", "C")
                    .withValue("a.d", "D")
                    .build()
        when:
            config = config
                    .withValue("a.b.c", "X")
                    .withValue("a.c", "Y")
                    .withValue("a.d", ["Z"])
        then:
            config.toMap() == [
                    a: [
                            b: [c: "X"],
                            c: "Y",
                            d: ["Z"]
                    ]
            ]
    }

    def "should fail adding list item with invalid index"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", ["c", "d"])
                    .build()
        when:
            config.withValue("a.b[3]", "e")
        then:
            thrown(MissingConfigValueException)

        when:
            config.withValue("a.b[-1]", "e")
        then:
            thrown(InvalidConfigPathException)
    }
}
