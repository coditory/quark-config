package com.coditory.quark.config.builder

import com.coditory.quark.config.Config
import com.coditory.quark.config.InvalidConfigPathException
import com.coditory.quark.config.MissingConfigValueException
import spock.lang.Specification

class PutIfMissingSpec extends Specification {
    def "should add values to an empty config"() {
        when:
            Config config = Config.builder()
                    .putIfMissing("a.b.c", "ABC")
                    .putIfMissing("a.d", "AD")
                    .putIfMissing("e", ["E0", "E1"])
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

    def "should not overwrite previously added values"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.d.e", "ADE")
                    .put("e", ["E0", "E1"])
                    .build()
        when:
            Config copy = Config.builder(config)
                    .putIfMissing("a.b.c", "X")
                    .putIfMissing("a.d", "Y")
                    .putIfMissing("e[1]", "Z")
                    .build()
        then:
            config.toMap() == copy.toMap()
    }

    def "should add new values to list of values"() {
        given:
            Config config = Config.builder()
                    .put("a.b", [[c: "C"], "e"])
                    .build()
        when:
            config = Config.builder(config)
                    .putIfMissing("a.b[0].d", "D")
                    .putIfMissing("a.b[0].e[0][0]", "E00")
                    .putIfMissing("a.b[2]", "3")
                    .build()
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
                    .put("a.b", ["c", "d"])
                    .build()
        when:
            Config copy = Config.builder(config)
                    .putIfMissing("a.b.c.d", "D")
                    .build()
        then:
            config.toMap() == copy.toMap()
    }

    def "should not replace nested value with list value"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c.d", "d")
                    .build()
        when:
            Config copy = Config.builder(config)
                    .putIfMissing("a.b", ["c", "d"])
                    .build()
        then:
            config.toMap() == copy.toMap()
    }

    def "should not replace value with other value"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c.d", "d")
                    .put("a.b.e", "e")
                    .build()
        when:
            Config copy = Config.builder(config)
                    .putIfMissing("a.b.c.d", "X")
                    .putIfMissing("a.b.e.d", "Y")
                    .build()
        then:
            config.toMap() == copy.toMap()
    }

    def "should fail adding list item with invalid index"() {
        given:
            Config config = Config.builder()
                    .put("a.b", ["c", "d"])
                    .build()
        when:
            Config.builder(config)
                    .putIfMissing("a.b[3]", "e")
                    .build()
        then:
            thrown(MissingConfigValueException)

        when:
            Config.builder(config)
                    .putIfMissing("a.b[-1]", "e")
                    .build()
        then:
            thrown(InvalidConfigPathException)
    }
}
