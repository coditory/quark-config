package com.coditory.configio.parsing

import com.coditory.configio.Config
import com.coditory.configio.api.ConfigValueConversionException
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

    private Short parse(String value) {
        String name = "value"
        return Config.fromMap(Map.of(name, value))
                .getAs(Short, name)
    }
}
