package com.coditory.quark.config

import spock.lang.Specification

import static ConfigProfiles.resolver

class ConfigProfileResolutionSpec extends Specification {
    def "should fail on not allowed profile"() {
        when:
            resolver()
                    .allowedProfiles("dev", "test")
                    .resolve("--profile", "other")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Invalid profiles: [other]"
    }

    def "should pass on allowed profile"() {
        when:
            List<String> profiles = resolver()
                    .allowedProfiles("dev", "test", "prod")
                    .resolve("--profile", "dev,test")
                    .getValues()
        then:
            profiles == ["dev", "test"]
    }

    def "should pass on allowed profile"() {
        when:
            List<String> profiles = resolver()
                    .allowedProfiles("dev", "test", "prod")
                    .resolve("--profile", "dev,test")
                    .getValues()
        then:
            profiles == ["dev", "test"]
    }

    def "should fail on exclusive profiles"() {
        when:
            resolver()
                    .exclusiveProfiles("dev", "test", "prod")
                    .resolve("--profile", "dev,test")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Detected exclusive profiles: [dev, test]"
    }

    def "should pass on non-exclusive profiles"() {
        when:
            List<String> profiles = resolver()
                    .exclusiveProfiles("dev", "test", "prod")
                    .resolve("--profile", "dev,other")
                    .getValues()
        then:
            profiles == ["dev", "other"]
    }

    def "should return default profiles"() {
        when:
            List<String> profiles = resolver()
                    .defaultProfiles("dev")
                    .resolve("--some-param", "abc")
                    .getValues()
        then:
            profiles == ["dev"]
    }

    def "should return profiles from args"() {
        when:
            List<String> profiles = resolver()
                    .defaultProfiles("dev")
                    .resolve("--profile", "test")
                    .getValues()
        then:
            profiles == ["test"]
    }

    def "should use custom profile arg name"() {
        when:
            List<String> profiles = resolver()
                    .profileArgName("profiles")
                    .resolve("--profiles", "test")
                    .getValues()
        then:
            profiles == ["test"]
    }

    def "should use alias for profile arg name"() {
        when:
            List<String> profiles = resolver()
                    .addArgsAlias("p", "profile")
                    .resolve("-p", "test")
                    .getValues()
        then:
            profiles == ["test"]
    }

    def "should use arg mapping for profile arg name"() {
        when:
            List<String> profiles = resolver()
                    .addArgsMapping(["--prod"], ["--profile", "prod"])
                    .resolve("--prod")
                    .getValues()
        then:
            profiles == ["prod"]
    }

    def "should validate profile count"() {
        when:
            List<String> profiles = resolver()
                    .expectedProfileCount(2)
                    .resolve("--profile", "dev,test")
                    .getValues()
        then:
            profiles == ["dev", "test"]

        when:
            resolver()
                    .expectedProfileCount(3)
                    .resolve("--profile", "dev,test")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Too little active profiles"
    }

    def "should map profiles"() {
        when:
            List<String> profiles = resolver()
                    .profilesMapper({ p -> p.collect { it + "-mapped" } })
                    .resolve("--profile", "dev,test")
                    .getValues()
        then:
            profiles == ["dev-mapped", "test-mapped"]
    }
}
