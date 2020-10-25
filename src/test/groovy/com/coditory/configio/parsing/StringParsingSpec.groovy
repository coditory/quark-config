package com.coditory.configio.parsing

import com.coditory.configio.Config
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

    private String parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .getAs(String, name)
    }
}
