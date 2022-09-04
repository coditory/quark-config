package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

class InstantParsingSpec extends Specification {
    @Unroll
    def "should parse Instant value: #value"() {
        expect:
            parse(value) == expected
        where:
            value                     || expected
            "2007-12-03T10:15:30.00Z" || Instant.parse("2007-12-03T10:15:30.00Z")
            "2007-12-03T10:15:30Z"    || Instant.parse("2007-12-03T10:15:30.00Z")
            "2007-12-03T10:15:00Z"    || Instant.parse("2007-12-03T10:15:00.00Z")
    }

    @Unroll
    def "should not parse invalid Instant value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << [
                    "2007-12-03T10:15:30.00",
                    "2007-12-03T10:15Z",
                    "2007-12-03"
            ]
    }

    def "should parse List of Instants"() {
        expect:
            parseList(["2007-12-03T10:15:30Z", "2007-12-03T10:15:30.00Z"]) == [
                    Instant.parse("2007-12-03T10:15:30.00Z"),
                    Instant.parse("2007-12-03T10:15:30.00Z")
            ]
    }

    def "should not parse a list of invalid Instants"() {
        when:
            parseList(["2007-12-03T10:15:30.00", "2007-12-03"])
        then:
            thrown(ConfigValueConversionException)
    }

    private Instant parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(Instant, name)
    }

    private List<Instant> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(Instant, name)
    }
}
