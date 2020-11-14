package com.coditory.config


import spock.lang.Specification

class ConfigWithValuesSpec extends Specification {
    def "should return sub config overridden with other config"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "AD")
                    .withValue("e", "E")
                    .build()
            Config other = Config.builder()
                    .withValue("a.b", "AB")
                    .withValue("a.x", "AX")
                    .build()
        when:
            Config result = config.withValues(other)
        then:
            result.toMap() == [
                    a: [
                            b: "AB",
                            d: "AD",
                            x: "AX"
                    ],
                    e: "E"
            ]
    }
}
