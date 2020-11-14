package com.coditory.config.parsing

import com.coditory.config.Config
import com.coditory.config.ConfigValueConversionException
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

    private Float parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .getAs(Float, name)
    }
}
