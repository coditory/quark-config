package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalTime

class LocalTimeParsingSpec extends Specification {
    @Unroll
    def "should parse LocalTime value: #value"() {
        expect:
            parse(value) == expected
        where:
            value         || expected
            "10:15:30.01" || LocalTime.parse("10:15:30.01")
            "10:15:30"    || LocalTime.parse("10:15:30.00")
            "10:15"       || LocalTime.parse("10:15:00.00")
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
                    "10:15:30.00Z",
                    "10:15:",
                    "10",
            ]
    }

    def "should parse List of LocalTimes"() {
        expect:
            parseList(["10:15:30", "10:15:30.00"]) == [
                    LocalTime.parse("10:15:30.00"),
                    LocalTime.parse("10:15:30.00")
            ]
    }

    def "should not parse a list of invalid LocalTimes"() {
        when:
            parseList(["2007-12-03T10:15:30.00", "2007-12-03"])
        then:
            thrown(ConfigValueConversionException)
    }

    private LocalTime parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(LocalTime, name)
    }

    private List<LocalTime> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(LocalTime, name)
    }
}
