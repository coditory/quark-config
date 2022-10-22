package com.coditory.quark.config.builder

import com.coditory.quark.config.Config
import spock.lang.Specification

class PutAllIfMissingSpec extends Specification {
    def "should return sub config merged with other config"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.d", "AD")
                    .put("e", "E")
                    .build()
            Config other = Config.builder()
                    .put("a.b.d", "ABD")
                    .put("a.x", "AX")
                    .build()
        when:
            Config result = Config.builder(config)
                    .putAllIfMissing(other)
                    .build()
        then:
            result.toMap() == [
                    a: [
                            b: [
                                    c: "ABC",
                                    d: "ABD"
                            ],
                            d: "AD",
                            x: "AX"
                    ],
                    e: "E"
            ]
    }

    def "should not overwrite leaf values"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.d", "AD")
                    .put("e", "E")
                    .build()
            Config other = Config.builder()
                    .put("a.b.c", "X")
                    .put("a.b", "Y")
                    .put("e", "Z")
                    .build()
        when:
            Config result = Config.builder(config)
                    .putAllIfMissing(other)
                    .build()
        then:
            result.toMap() == [
                    a: [
                            b: [c: "ABC"],
                            d: "AD"
                    ],
                    e: "E"
            ]
    }

    def "should not merge list values as leaves"() {
        given:
            Config config = Config.builder()
                    .put("a.b[0].c", "AB0C")
                    .put("a.b[1]", "AB1")
                    .build()
            Config other = Config.builder()
                    .put("a.b[0].c", "AB0C")
                    .put("a.b[0].d", "AB0D")
                    .put("a.b[1]", "AB1")
                    .put("a.b[2]", "AB2")
                    .build()
        when:
            Config result = Config.builder(config)
                    .putAllIfMissing(other)
                    .build()
        then:
            result.toMap() == [
                    a: [
                            b: [[c: "AB0C"], "AB1"]
                    ]
            ]
    }
}
