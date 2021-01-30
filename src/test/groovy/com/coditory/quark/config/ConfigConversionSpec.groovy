package com.coditory.quark.config

import spock.lang.Specification

class ConfigConversionSpec extends Specification {
    def "should convert config to a flat map"() {
        given:
            Config config = Config.of([
                    a: [
                            b: "B",
                            c: [[d: 123], "C1"]
                    ],
                    e: true,
                    f: ["F0", "F1"]
            ])
        when:
            Map<String, Object> flatMap = config.toFlatMap()
        then:
            flatMap == [
                    'a.b'     : "B",
                    'a.c[0].d': 123,
                    'a.c[1]'  : "C1",
                    'e'       : true,
                    'f[0]'    : "F0",
                    'f[1]'    : "F1"
            ]
    }

    def "should convert config to a nested map"() {
        given:
            Map<String, Object> rawMap = [
                    a: [
                            b: "B",
                            c: [[d: 123], "C1"]
                    ],
                    e: true,
                    f: ["F0", "F1"]
            ]
            Config config = Config.of(rawMap)
        when:
            Map<String, Object> map = config.toMap()
        then:
            map == rawMap
    }
}
