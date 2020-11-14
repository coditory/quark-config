package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class BooleanParsingSpec extends Specification {
    @Unroll
    def "should parse Boolean value: #value"() {
        expect:
            parse(value) == expected
        where:
            value   || expected
            "TrUe"  || true
            "FaLsE" || false
    }

    @Unroll
    def "should not parse invalid Boolean value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << ["TrUee", "fFaLsE", "1", "2"]
    }

    private Boolean parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Boolean, name)
    }
}
