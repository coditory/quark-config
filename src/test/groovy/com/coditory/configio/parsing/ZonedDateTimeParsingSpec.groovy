package com.coditory.configio.parsing

import com.coditory.configio.Config
import com.coditory.configio.api.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.ZonedDateTime

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class ZonedDateTimeParsingSpec extends Specification {
    @Unroll
    def "should parse ZonedDateTime value: #value"() {
        expect:
            parse(value) == expected
        where:
            value                       || expected
            "2011-12-03T10:15:30+01:00" || zonedDateTime("2011-12-03T10:15:30+01:00")
            "2011-12-03T10:15:30+01"    || zonedDateTime("2011-12-03T10:15:30+01")
            "2007-12-03T10:15:00+01"    || zonedDateTime("2007-12-03T10:15:00+01")
    }

    @Unroll
    def "should not parse invalid ZonedDateTime value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << [
                    "2011-12-03T10:15:30",
                    "2011-12-03T10:15:30+100",
                    "2011-12-03T10:15+1"
            ]
    }

    static private ZonedDateTime zonedDateTime(String value) {
        return ZonedDateTime.parse(value, ISO_OFFSET_DATE_TIME)
    }

    private ZonedDateTime parse(String value) {
        String name = "value"
        return Config.fromMap(Map.of(name, value))
                .getAs(ZonedDateTime, name)
    }
}
