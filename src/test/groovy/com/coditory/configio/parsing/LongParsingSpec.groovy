package com.coditory.configio.parsing

import com.coditory.configio.Config
import com.coditory.configio.api.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class LongParsingSpec extends Specification {
    @Unroll
    def "should parse Long value: #value"() {
        expect:
            parse(value) == expected
        where:
            value    || expected
            "0"      || 0L
            "-1"     || -1L
            "1"      || 1L
            "123123" || 123123L
    }

    @Unroll
    def "should not parse invalid Long value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << ["100a", "-a100", "1.0", "0.1"]
    }

    private Long parse(String value) {
        String name = "value"
        return Config.fromMap(Map.of(name, value))
                .getAs(Long, name)
    }
}
