package com.coditory.quark.config.builder

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigRemoveOptions
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParentLists
import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParentObjects
import static com.coditory.quark.config.ConfigRemoveOptions.removeEmptyParents

class RemoveValueSpec extends Specification {
    def "should remove values"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.b.d", "ABD")
                    .put("a.d", "AD")
                    .build()
        when:
            config = remove(config, "a.b.c")
        then:
            !config.contains("a.b.c")
            config.contains("a.b.d")

        when:
            config = remove(config, "a.b")
        then:
            !config.contains("a.b.d")
    }

    def "should remove values and leave empty parents"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.d", "AD")
                    .put("e.c[0]", "EC0")
                    .build()
        when:
            config = remove(config, "a.b.c")
            config = remove(config, "e.c[0]")
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
                    .put("a.b.c", "ABC")
                    .put("a.d", "AD")
                    .put("e.c[0]", "EC0")
                    .build()
        when:
            config = remove(config, "a.b.c", options)
            config = remove(config, "e.c[0]", options)
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
                    .put("a.b", "AB")
                    .put("a.b.c", "ABC")
                    .put("a.b[0].c", "AB0C")
                    .put("a.b[0].d", "AB0D")
                    .put("a.b[1]", "AB1")
                    .build()
        when:
            config = remove(config, "a.b[0].d")
        then:
            config.getString("a.b[0].c", "AB0C")
            !config.contains("a.b[0].d")

        when:
            config = remove(config, "a.b[0]")
        then:
            config.getString("a.b[0]", "AB1")
    }

    def "should not throw error when removing non existent value"() {
        given:
            Config config = Config.builder()
                    .put("a.b", "AB")
                    .put("a.b.c", "ABC")
                    .put("a.b[0]", "AB0")
                    .build()
            Config copy = config

        when:
            config = remove(config, "a.b[1]")
            config = remove(config, "a.b.c.d")
            config = remove(config, "x")

        then:
            config == copy
    }

    private Config remove(Config config, String path) {
        return Config.builder(config)
                .remove(path)
                .build()
    }

    private Config remove(Config config, String path, ConfigRemoveOptions options) {
        return Config.builder(config)
                .remove(path, options)
                .build()
    }
}
