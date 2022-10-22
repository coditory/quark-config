package com.coditory.quark.config.builder

import com.coditory.quark.config.Config
import com.coditory.quark.config.InvalidConfigPathException
import com.coditory.quark.config.MissingConfigValueException
import spock.lang.Specification

class PutValueSpec extends Specification {
    def "should add values to an empty config"() {
        when:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.d", "AD")
                    .put("e", ["E0", "E1"])
                    .build()
        then:
            config.toMap() == [
                    a: [
                            b: [c: "ABC"],
                            d: "AD"
                    ],
                    e: ["E0", "E1"]
            ]
    }

    def "should add empty values to an empty config"() {
        when:
            Config config = Config.builder()
                    .put("a", [:])
                    .put("b", [])
                    .put("c", [[], [:]])
                    .build()
        then:
            config.toMap() == [
                    a: [:],
                    b: [],
                    c: [[], [:]]
            ]
    }

    def "should overwrite previously added values"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.d.e", "ADE")
                    .put("e", ["E0", "E1"])
                    .build()
        when:
            config = Config.builder(config)
                    .put("a.b.c", "X")
                    .put("a.d", "Y")
                    .put("e[1]", "Z")
                    .build()
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
                    .put("a.b", [[c: "ABC"], "d", "e"])
                    .build()
        when:
            config = Config.builder(config)
                    .put("a.b[0].c", "C")
                    .put("a.b[0].d", "D")
                    .put("a.b[0].e[0][0]", "E00")
                    .put("a.b[1]", [z: "Z"])
                    .put("a.b[3]", "3")
                    .build()
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
                    .put("a.b", ["c", "d"])
                    .build()
        when:
            config = Config.builder(config)
                    .put("a.b.c.d", "D")
                    .build()
        then:
            config.toMap() == [
                    a: [b: [c: [d: "D"]]]
            ]
    }

    def "should replace nested value with list value"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c.d", "d")
                    .build()
        when:
            config = Config.builder(config)
                    .put("a.b", ["c", "d"])
                    .build()
        then:
            config.toMap() == [
                    a: [b: ["c", "d"]]
            ]
    }

    def "should replace value with other value"() {
        given:
            Config config = Config.builder()
                    .put("a.b", "B")
                    .put("a.c", "C")
                    .put("a.d", "D")
                    .build()
        when:
            config = Config.builder(config)
                    .put("a.b.c", "X")
                    .put("a.c", "Y")
                    .put("a.d", ["Z"])
                    .build()
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
                    .put("a.b", ["c", "d"])
                    .build()
        when:
            Config.builder(config)
                    .put("a.b[3]", "e")
                    .build()
        then:
            thrown(MissingConfigValueException)

        when:
            Config.builder(config)
                    .put("a.b[-1]", "e")
                    .build()
        then:
            thrown(InvalidConfigPathException)
    }
}
