package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class IntegerParsingSpec extends Specification {
    @Unroll
    def "should parse Integer value: #value"() {
        expect:
            parse(value) == expected
        where:
            value    || expected
            "0"      || 0
            "-1"     || -1
            "1"      || 1
            "123123" || 123123
    }

    @Unroll
    def "should not parse invalid Integer value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << ["100a", "-a100", "1.0", "0.1"]
    }

    def "should parse List of Integers"() {
        expect:
            parseList(["0", "-1", "1"]) == [
                    0, -1, 1
            ]
    }

    def "should not parse a list of invalid Integers"() {
        when:
            parseList(["100a", "1"])
        then:
            thrown(ConfigValueConversionException)
    }

    private Integer parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Integer, name)
    }

    private List<Integer> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(Integer, name)
    }
}
