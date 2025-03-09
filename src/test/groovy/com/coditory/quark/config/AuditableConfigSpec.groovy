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
            config.failOnUnusedProperties()
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
            config.failOnUnusedProperties()
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
            config.failOnUnusedProperties()
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
            config.failOnUnusedProperties()
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\nd.e\nd.f"
        and:
            config.getUnusedProperties() == Config.of(d: [e: "DE", f: "DF"])

        when:
            config.getSubConfig("d")
            config.failOnUnusedProperties()
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
            config.failOnUnusedProperties()
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\na[1]"
        and:
            config.getUnusedProperties() == Config.of("a", ["B"])

        when:
            config.getString("a[1]")
            config.failOnUnusedProperties()
        then:
            noExceptionThrown()
            config.getUnusedProperties() == Config.empty()
    }

    def "should detected all unused config properties in passed config consumer"() {
        given:
            Config config = Config.of(a: "A", b: "B")
        when:
            config.consumeAuditable {
                it.getString("a")
            }
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\nb"

        when:
            config.consumeAuditable {
                it.getString("a")
                it.getString("b")
            }
        then:
            noExceptionThrown()
    }

    def "should detected all unused config properties in passed config mapper"() {
        given:
            Config config = Config.of(a: "A", b: "B")
        when:
            String result = config.mapAuditable {
                it.getString("a")
            }
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\nb"
            result == null

        when:
            result = config.mapAuditable {
                it.getString("a") + it.getString("b")
            }
        then:
            noExceptionThrown()
            result == "AB"
    }

    def "should detected all unused config properties from subconfig"() {
        given:
            Config config = Config.of(
                    a: "A",
                    b: [
                            c: "C",
                            d: "D",
                            e: [
                                    f: "F",
                                    g: "G"
                            ]
                    ])
        when:
            config.mapAuditableSubConfig("b") {
                it.getString("c")
                it.getString("e.g")
                "Test"
            }
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\nb.d\nb.e.f"

        when:
            config.mapAuditableSubConfig("b") {
                it.getString("c")
                it.getString("d")
                it.getString("e.f")
                it.getString("e.g")
            }
        then:
            noExceptionThrown()
    }

    def "should recognize nested auditable subconfigs"() {
        given:
            Config config = Config.of(
                    a: "A",
                    b: [
                            e: [
                                    f: "F",
                                    g: "G"
                            ]
                    ])
        when:
            config.mapAuditableSubConfig("b") {
                it.mapAuditableSubConfig("e") {
                    it.getString("g")
                }
            }
        then:
            ConfigUnusedPropertiesException e = thrown(ConfigUnusedPropertiesException)
            e.message == "Detected unused config properties:\nb.e.f"

        when:
            config.mapAuditableSubConfig("b") {
                it.mapAuditableSubConfig("e") {
                    it.getString("g")
                    it.getString("f")
                }
            }
        then:
            noExceptionThrown()

        when:
            config.mapAuditableSubConfig("b") {
                it.mapAuditableSubConfigOrNull("e") {
                    it.getString("g")
                    it.getString("f")
                }
            }
        then:
            noExceptionThrown()

        when:
            config.mapAuditableSubConfig("b") {
                it.mapAuditableSubConfigOrEmpty("e") {
                    it.getString("g")
                    it.getString("f")
                }
            }
        then:
            noExceptionThrown()
    }
}
