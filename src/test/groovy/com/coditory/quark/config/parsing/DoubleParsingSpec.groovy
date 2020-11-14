package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class DoubleParsingSpec extends Specification {
    @Unroll
    def "should parse Double value: #value"() {
        expect:
            Double.compare(parse(value), expected) == 0
        where:
            value       || expected
            "0.0001"    || 0.0001d
            "-0.0001"   || -0.0001d
            "123123.03" || 123123.03d
    }

    @Unroll
    def "should not parse invalid Double value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << ["0.0001a", "-a0.0001"]
    }

    private Double parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .getAs(Double, name)
    }
}
