package com.coditory.quark.config

import spock.lang.Specification

class NumberConversionSpec extends Specification {
    def "should convert int to long"() {
        given:
            Config config = Config.builder()
                    .putAll(a: value)
                    .build()
        when:
            Long result = config.getLong("a")
        then:
            result == expected
        where:
            value  || expected
            0      || 0L
            -1     || -1L
            12     || 12L
            123123 || 123123L
    }

    def "should not convert float to long"() {
        given:
            Config config = Config.builder()
                    .putAll(a: 1.2)
                    .build()
        when:
            config.getLong("a")
        then:
            thrown(ConfigValueConversionException)
    }

    def "should convert float or int to double"() {
        given:
            Config config = Config.builder()
                    .putAll(a: value)
                    .build()
        when:
            Double result = config.getDouble("a")
        then:
            result == expected
        where:
            value    || expected
            0        || 0d
            -1       || -1d
            1.2      || 1.2d
            -123.123 || -123.123d
            12       || 12d
            123123   || 123123d
    }
}
