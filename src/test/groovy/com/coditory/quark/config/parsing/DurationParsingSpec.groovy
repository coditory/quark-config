package com.coditory.quark.config.parsing

import com.coditory.quark.config.Config
import com.coditory.quark.config.ConfigValueConversionException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

class DurationParsingSpec extends Specification {
    @Unroll
    def "should parse Duration value: #value"() {
        expect:
            parse(value) == expected
        where:
            value       || expected
            "PT20.345S" || Duration.parse("PT20.345S")
            "-PT-6H+3M" || Duration.parse("-PT-6H+3M")
    }

    @Unroll
    def "should parse short Duration time value: #value"() {
        expect:
            parse(value) == expected
        where:
            value    || expected
            "10ms"   || Duration.parse("PT0.01S")
            "10.5ms" || Duration.parse("PT0.0105S")
            "1.5s"   || Duration.parse("PT1.5S")
            "1m"     || Duration.parse("PT1M")
            "2h"     || Duration.parse("PT2H")
    }

    @Unroll
    def "should not parse invalid Duration value: #value"() {
        when:
            parse(value)
        then:
            thrown(ConfigValueConversionException)
        where:
            value << [
                    "1.5m" ,
                    "1d",
                    "10.5mss",
                    "1.0.5ms"
            ]
    }

    private Duration parse(String value) {
        String name = "value"
        return Config.of(Map.of(name, value))
                .getAs(Duration, name)
    }
}
