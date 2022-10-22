package com.coditory.quark.config

import spock.lang.Specification

class AuditableConfigSpec extends Specification {
    def "should detected all unused config properties"() {
        given:
            AuditableConfig config = Config.builder()
                    .putAll(a: "A", b: "B")
                    .build()
                    .auditable()
        when:
            config.throwErrorOnUnusedProperties()
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\na\nb"
        and:
            config.getUnusedProperties() == Config.of(a: "A", b: "B")
    }

    def "should detected single unused config property"() {
        given:
            AuditableConfig config = Config.builder()
                    .putAll(a: "A", b: "B")
                    .build()
                    .auditable()
        when:
            config.getString("a")
            config.throwErrorOnUnusedProperties()
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\nb"
        and:
            config.getUnusedProperties() == Config.of(b: "B")
    }

    def "should not detected unused config properties when all were used"() {
        given:
            AuditableConfig config = Config.builder()
                    .putAll(a: "A", b: "B")
                    .build()
                    .auditable()
        when:
            config.getString("a")
            config.getString("b")
            config.throwErrorOnUnusedProperties()
        then:
            noExceptionThrown()
            config.getUnusedProperties() == Config.empty()
    }

    def "should detect unused nested config properties"() {
        given:
            AuditableConfig config = Config.builder()
                    .put("a.b", "AB")
                    .put("a.c", "AA")
                    .put("d.e", "DE")
                    .put("d.f", "DF")
                    .build()
                    .auditable()
        when:
            config.getSubConfig("a")
            config.throwErrorOnUnusedProperties()
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\nd.e\nd.f"
        and:
            config.getUnusedProperties() == Config.of(d: [e: "DE", f: "DF"])

        when:
            config.getSubConfig("d")
            config.throwErrorOnUnusedProperties()
        then:
            noExceptionThrown()
            config.getUnusedProperties() == Config.empty()
    }

    def "should detect unused list config properties"() {
        given:
            AuditableConfig config = Config.builder()
                    .put("a", ["A", "B"])
                    .build()
                    .auditable()
        when:
            config.getString("a[0]")
            config.throwErrorOnUnusedProperties()
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\na[1]"
        and:
            config.getUnusedProperties() == Config.of("a", ["B"])

        when:
            config.getString("a[1]")
            config.throwErrorOnUnusedProperties()
        then:
            noExceptionThrown()
            config.getUnusedProperties() == Config.empty()
    }
}
