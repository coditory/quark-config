package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class LocalDateParsingSpec extends Specification {
    @Unroll
    def "should parse LocalDate value: #value"() {
        expect:
            parse("2007-12-03") == LocalDate.parse("2007-12-03")
    }

    @Unroll
    def "should not parse invalid LocalDate value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << [
                    "2007-12-03T10:15:30.00Z",
                    "2007-12-03T10:15",
                    "2007-12"
            ]
    }

    def "should parse List of LocalDates"() {
        expect:
            parseList(["2007-12-03", "2007-12-04"]) == [
                    LocalDate.parse("2007-12-03"),
                    LocalDate.parse("2007-12-04")
            ]
    }

    def "should not parse a list of invalid LocalDates"() {
        when:
            parseList(["2007-12-03T10:15:30.00", "2007-12-03"])
        then:
            thrown(ConfigValueConversionException)
    }

    private LocalDate parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(LocalDate, name)
    }

    private List<LocalDate> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(LocalDate, name)
    }
}
