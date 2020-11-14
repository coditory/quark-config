package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class LocaleParsingSpec extends Specification {
    @Unroll
    def "should parse Locale value: #value"() {
        expect:
            parse(value) == expected
        where:
            value   || expected
            "pl_PL" || new Locale("pl", "PL")
            "pl-PL" || new Locale("pl", "PL")
            "pl"    || new Locale("pl")
    }

    @Unroll
    def "should not parse invalid Locale value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << [
                    "plll",
                    "pl-PLLLLL",
                    "xyz"
            ]
    }

    private Locale parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Locale, name)
    }
}
