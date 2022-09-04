package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class ShortParsingSpec extends Specification {
    @Unroll
    def "should parse Short value: #value"() {
        expect:
            parse(value) == expected
        where:
            value || expected
            "0"   || 0
            "-1"  || -1
            "1"   || 1
            "123" || 123
    }

    @Unroll
    def "should not parse invalid Short value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << ["100a", "-a100", "1.0", "0.1"]
    }

    def "should parse List of Shorts"() {
        expect:
            parseList(["0", "-1", "1"]) == [
                    0, -1, 1
            ] as List<Short>
    }

    def "should not parse a list of invalid Shorts"() {
        when:
            parseList(["100a", "1"])
        then:
            thrown(ConfigValueConversionException)
    }

    private Short parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Short, name)
    }

    private List<Short> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(Short, name)
    }
}
