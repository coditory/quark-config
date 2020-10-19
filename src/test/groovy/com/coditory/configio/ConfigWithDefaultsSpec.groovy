package com.coditory.configio


import spock.lang.Specification

class ConfigWithDefaultsSpec extends Specification {
    def "should return sub config merged with other config"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e", "E")
                    .build()
            Config other = Config.builder()
                    .withValue("a.b.d", "ABD")
                    .withValue("a.x", "AX")
                    .build()
        when:
            Config result = config.addDefaults(other)
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
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e", "E")
                    .build()
            Config other = Config.builder()
                    .withValue("a.b.c", "X")
                    .withValue("a.b", "Y")
                    .withValue("e", "Z")
                    .build()
        when:
            Config result = config.addDefaults(other)
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
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[1]", "AB1")
                    .build()
            Config other = Config.builder()
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[0].d", "AB0D")
                    .withValue("a.b[1]", "AB1")
                    .withValue("a.b[2]", "AB2")
                    .build()
        when:
            Config result = config.addDefaults(other)
        then:
            result.toMap() == [
                    a: [
                            b: [[c: "AB0C"], "AB1"]
                    ]
            ]
    }
}
