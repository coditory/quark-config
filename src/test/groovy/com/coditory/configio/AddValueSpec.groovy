package com.coditory.configio


import com.coditory.configio.api.InvalidConfigPathException
import spock.lang.Specification
import spock.lang.Unroll

class AddValueSpec extends Specification {
    def "should not overwrite previously defined value using builder"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "AB")
                    .withValue("a.b.c", "ABC")
                    .withValue("a.b[0]", "A0")
                    .build()
        expect:
            config.toMap() == [a: [b: "AB"]]
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
