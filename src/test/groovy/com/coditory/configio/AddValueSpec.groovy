package com.coditory.configio

import com.coditory.configio.api.InvalidConfigPathException
import com.coditory.configio.api.MissingConfigValueException
import spock.lang.Specification

class AddValueSpec extends Specification {
    def "should add values to an empty config"() {
        given:
            Config config = Config.empty()
        when:
            config = config
                    .add("a.b.c", "ABC")
                    .add("a.d", "AD")
                    .add("e", ["E0", "E1"])
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
                    .add("a.b.c", "X")
                    .add("a.d", "Y")
                    .add("e[1]", "Z")
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
                    .add("a.b[0].c", "C")
                    .add("a.b[0].d", "D")
                    .add("a.b[0].e[0][0]", "E00")
                    .add("a.b[1]", [z: "Z"])
                    .add("a.b[3]", "3")
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
                    .add("a.b.c.d", "D")
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
                    .add("a.b", ["c", "d"])
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
                    .add("a.b.c", "X")
                    .add("a.c", "Y")
                    .add("a.d", ["Z"])
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
            config.add("a.b[3]", "e")
        then:
            thrown(MissingConfigValueException)

        when:
            config.add("a.b[-1]", "e")
        then:
            thrown(InvalidConfigPathException)
    }
}
