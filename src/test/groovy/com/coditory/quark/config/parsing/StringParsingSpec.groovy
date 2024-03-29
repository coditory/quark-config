package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import spock.lang.Specification
import spock.lang.Unroll

class StringParsingSpec extends Specification {
    @Unroll
    def "should parse String value: #value"() {
        expect:
            parse(value) == expected
        where:
            value                     || expected
            "2007-12-03T10:15:30.00Z" || "2007-12-03T10:15:30.00Z"
            "1234"                    || "1234"
            "some text"               || "some text"
    }

    def "should parse List of Strings"() {
        expect:
            parseList(["abc", "Def123"]) == [
                    "abc", "Def123"
            ]
    }

    private String parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(String, name)
    }

    private List<String> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(String, name)
    }
}
