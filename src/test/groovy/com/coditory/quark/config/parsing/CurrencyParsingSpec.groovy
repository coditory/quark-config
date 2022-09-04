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

    def "should parse List of Currencies"() {
        expect:
            parseList(["PLN", "USD"]) == [
                    Currency.getInstance("PLN"),
                    Currency.getInstance("USD")
            ]
    }

    def "should not parse a list of invalid Currencies"() {
        when:
            parseList(["PLNN", "PLN"])
        then:
            thrown(ConfigValueConversionException)
    }

    private Currency parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Currency, name)
    }

    private List<Currency> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(Currency, name)
    }
}
