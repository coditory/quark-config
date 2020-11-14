package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class ByteParsingSpec extends Specification {
    @Unroll
    def "should parse Byte value: #value"() {
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
    def "should not parse invalid Byte value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << ["100a", "-a100", "1.0", "0.1"]
    }

    private Byte parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Byte, name)
    }
}
