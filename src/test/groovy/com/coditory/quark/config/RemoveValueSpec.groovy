package com.coditory.quark.config

import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParentLists
import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParentObjects
import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParents

class RemoveValueSpec extends Specification {
    def "should remove values"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.b.d", "ABD")
                    .withValue("a.d", "AD")
                    .build()
        when:
            config = config.remove("a.b.c")
        then:
            !config.contains("a.b.c")
            config.contains("a.b.d")

        when:
            config = config.remove("a.b")
        then:
            !config.contains("a.b.d")
    }

    def "should remove values and leave empty parents"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e.c[0]", "EC0")
                    .build()
        when:
            config = config.remove("a.b.c")
            config = config.remove("e.c[0]")
        then:
            config.toMap() == [
                    a: [d: "AD", b: [:]],
                    e: [c: []]
            ]
    }

    @Unroll
    def "should remove values and #name"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e.c[0]", "EC0")
                    .build()
        when:
            config = config.remove("a.b.c", options)
            config = config.remove("e.c[0]", options)
        then:
            config.toMap() == result

        where:
            name                   | options                    | result
            "empty parents"        | removeEmptyParents()       | [a: [d: "AD"]]
            "empty parent objects" | removeEmptyParentObjects() | [a: [d: "AD"], e: [c: []]]
            "empty parent lists"   | removeEmptyParentLists()   | [a: [d: "AD", b: [:]], e: [:]]
    }

    def "should remove values from a list"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "AB")
                    .withValue("a.b.c", "ABC")
                    .withValue("a.b[0].c", "AB0C")
                    .withValue("a.b[0].d", "AB0D")
                    .withValue("a.b[1]", "AB1")
                    .build()
        when:
            config = config.remove("a.b[0].d")
        then:
            config.getString("a.b[0].c", "AB0C")
            !config.contains("a.b[0].d")

        when:
            config = config.remove("a.b[0]")
        then:
            config.getString("a.b[0]", "AB1")
    }

    def "should not throw error when removing non existent value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "AB")
                    .withValue("a.b.c", "ABC")
                    .withValue("a.b[0]", "AB0")
                    .build()
            Config copy = config

        when:
            config = config.remove("a.b[1]")
            config = config.remove("a.b.c.d")
            config = config.remove("x")

        then:
            config == copy
    }
}
