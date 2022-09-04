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

    def "should parse List of Booleans"() {
        expect:
            parseList(["TrUe", "true", "false"]) == [
                    true, true, false
            ]
    }

    def "should not parse a list of invalid Booleans"() {
        when:
            parseList(["TrUee", "true"])
        then:
            thrown(ConfigValueConversionException)
    }

    private Boolean parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Boolean, name)
    }

    private List<Boolean> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(Boolean, name)
    }
}
