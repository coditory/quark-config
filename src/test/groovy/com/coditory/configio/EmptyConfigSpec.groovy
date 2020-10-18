package com.coditory.configio

import spock.lang.Specification

class EmptyConfigSpec extends Specification {
    def "should use singleton for empty config"() {
        expect:
            Config.empty() == Config.empty()
        and:
            Config.empty() == Config.of(Map.of())
    }
}
