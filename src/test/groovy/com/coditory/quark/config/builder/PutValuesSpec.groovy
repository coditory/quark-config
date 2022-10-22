package com.coditory.quark.config.builder

import com.coditory.quark.config.Config
import spock.lang.Specification

class PutValuesSpec extends Specification {
    def "should return sub config overridden with other config"() {
        given:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.d", "AD")
                    .put("e", "E")
                    .build()
            Config other = Config.builder()
                    .put("a.b", "AB")
                    .put("a.x", "AX")
                    .build()
        when:
            Config result = Config.builder(config)
                    .putAll(other)
                    .build()
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
