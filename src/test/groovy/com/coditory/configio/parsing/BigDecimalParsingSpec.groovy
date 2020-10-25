package com.coditory.configio.parsing

import com.coditory.configio.Config
import com.coditory.configio.api.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class BigDecimalParsingSpec extends Specification {
    @Unroll
    def "should parse BigDecimal value: #value"() {
        expect:
            parse(value) == expected
        where:
            value       || expected
            "0.0001"    || new BigDecimal("0.0001")
            "-0.0001"   || new BigDecimal("-0.0001")
            "123123.03" || new BigDecimal("123123.03")
    }

    @Unroll
    def "should not parse invalid BigDecimal value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << ["0.0001a", "-a0.0001"]
    }

    private BigDecimal parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .getAs(BigDecimal, name)
    }
}
