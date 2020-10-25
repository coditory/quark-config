package com.coditory.configio.parsing

import com.coditory.configio.Config
import com.coditory.configio.api.ConfigValueConversionException
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

    private Integer parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .getAs(Integer, name)
    }
}
