package com.coditory.config.parsing

import com.coditory.config.Config
import com.coditory.config.ConfigValueConversionException
import com.coditory.config.ValueParser
import spock.lang.Specification

class CustomParserSpec extends Specification {
    def "should parse value using custom parser"() {
        given:
            Config config = Config.builder()
                    .withValueParser(ValueParser.forType(WrapperX, { new WrapperX(it) }))
                    .withValue("value", "someValue")
                    .build()
        when:
            WrapperX result = config.getAs(WrapperX, "value")
        then:
            result == new WrapperX("someValue")
    }

    def "should parse subtype value using custom parser"() {
        given:
            Config config = Config.builder()
                    .withValueParser(WrapperY, { new WrapperY(it) })
                    .withValue("value", "someValue")
                    .build()
        when:
            WrapperX result = config.getAs(WrapperX, "value")
        then:
            result == new WrapperY("someValue")
    }

    def "should obey parsers registration order"() {
        given:
            Config config = Config.builder()
                    .withValueParser(WrapperX, { new WrapperX(it + "1") })
                    .withValueParser(WrapperX, { new WrapperX(it + "2") })
                    .withValue("value", "someValue")
                    .build()
        when:
            WrapperX result = config.getAs(WrapperX, "value")
        then:
            result == new WrapperX("someValue1")
    }

    def "should not parse a subtype type with a supertype parser"() {
        given:
            Config config = Config.builder()
                    .withValueParser(WrapperX, { new WrapperX(it) })
                    .withValue("value", "someValue")
                    .build()
        when:
            config.getAs(WrapperY, "value")
        then:
            thrown(ConfigValueConversionException)
    }

    class WrapperX {
        private final String value;

        WrapperX(String value) {
            this.value = value
        }

        String getValue() {
            return value
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false
            WrapperX wrapperX = (WrapperX) o
            if (value != wrapperX.value) return false
            return true
        }

        int hashCode() {
            return (value != null ? value.hashCode() : 0)
        }
    }

    class WrapperY extends WrapperX {
        WrapperY(String value) {
            super(value)
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false
            WrapperY wrapperY = (WrapperY) o
            if (value != wrapperY.value) return false
            return true
        }

        int hashCode() {
            return (value != null ? value.hashCode() : 0)
        }
    }
}
