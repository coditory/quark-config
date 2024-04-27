package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

class LocalDateTimeParsingSpec extends Specification {
    @Unroll
    def "should parse LocalDateTime value: #value"() {
        expect:
            parse(value) == expected
        where:
            value                    || expected
            "2007-12-03T10:15:30.01" || LocalDateTime.parse("2007-12-03T10:15:30.01")
            "2007-12-03T10:15:30"    || LocalDateTime.parse("2007-12-03T10:15:30.00")
            "2007-12-03T10:15"       || LocalDateTime.parse("2007-12-03T10:15:00.00")
    }

    @Unroll
    def "should not parse invalid LocalDateTime value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << [
                    "2007-12-03T10:15:",
                    "2007-12",
                    "2007-12-03T00:00:00.00Z"
            ]
    }

    def "should parse List of LocalDateTimes"() {
        expect:
            parseList(["2007-12-03T10:15:30", "2007-12-03T10:15:30.00"]) == [
                    LocalDateTime.parse("2007-12-03T10:15:30.00"),
                    LocalDateTime.parse("2007-12-03T10:15:30.00")
            ]
    }

    def "should not parse a list of invalid LocalDateTimes"() {
        when:
            parseList(["2007-12-03T10:15:30.00", "2007-12-03"])
        then:
            thrown(ConfigValueConversionException)
    }

    private LocalDateTime parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .get(LocalDateTime, name)
    }

    private List<LocalDateTime> parseList(List<String> values) {
        String name = "value"
        return Config.of(Map.of(name, values))
                .getList(LocalDateTime, name)
    }
}
