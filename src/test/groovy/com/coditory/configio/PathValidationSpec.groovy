package com.coditory.configio


import com.coditory.configio.api.InvalidConfigPathException
import spock.lang.Specification
import spock.lang.Unroll

class PathValidationSpec extends Specification {
    @Unroll
    def "should accept valid config path: #path"() {
        when:
            Config.builder()
                    .withValue(path, "value")
                    .build()
        then:
            noExceptionThrown()
        where:
            path << [
                    "a[0]",
                    "a.b.c.d",
                    "a[0].b[0].c[0][0]",
                    "abcDefGhi0123"
            ]
    }

    @Unroll
    def "should throw error for invalid config path: #path"() {
        when:
            Config.builder()
                    .withValue(path, "value")
                    .build()
        then:
            thrown(InvalidConfigPathException)
        where:
            path << [
                    "a[x]",
                    "a[-1]",
                    "a[1]",
                    "a..b",
                    "a. .b"
            ]
    }
}
