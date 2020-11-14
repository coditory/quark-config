package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

class CurrencyParsingSpec extends Specification {
    @Unroll
    def "should parse Currency value: #value"() {
        expect:
            parse(value) == expected
        where:
            value || expected
            "PLN" || Currency.getInstance("PLN")
            "USD" || Currency.getInstance("USD")
    }

    @Unroll
    def "should not parse invalid Currency value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << [
                    "PLNN",
                    "PLX",
                    "123"
            ]
    }

    private Currency parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .getAs(Currency, name)
    }
}
