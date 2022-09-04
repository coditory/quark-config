package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class FloatParsingSpec extends Specification {
    @Unroll
    def "should parse Float value: #value"() {
        expect:
            Float.compare(parse(value), expected) == 0
        where:
            value       || expected
            "0.0001"    || 0.0001
            "-0.0001"   || -0.0001
            "123123.03" || 123123.03
    }

    @Unroll
    def "should not parse invalid Float value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << ["0.0001a", "-a0.0001"]
    }

    def "should parse List of Floats"() {
        expect:
            parseList(["0.0001", "-0.0001"]) == [
                    0.0001f, -0.0001f
            ]
    }

    def "should not parse a list of invalid Floats"() {
        when:
            parseList(["0.0001a", "1"])
        then:
            thrown(ConfigValueConversionException)
    }

    private Float parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Float, name)
    }

    private List<Float> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(Float, name)
    }
}
