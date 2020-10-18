package com.coditory.configio.parsing

import com.coditory.configio.Config
import com.coditory.configio.api.ConfigValueConversionException
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
        return Config.fromMap(Map.of(name, value))
            .getAs(Boolean, name)
    }
}
