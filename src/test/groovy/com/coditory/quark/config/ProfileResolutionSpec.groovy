package com.coditory.quark.config

import spock.lang.Specification

import static com.coditory.quark.config.Profiles.profilesResolver

class ProfileResolutionSpec extends Specification {
    def "should fail on not allowed profile"() {
        when:
            profilesResolver()
                    .withAllowedProfiles("dev", "test")
                    .resolve("--profile", "other")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Invalid profiles: [other]"
    }

    def "should pass on allowed profile"() {
        when:
            List<String> profiles = profilesResolver()
                    .withAllowedProfiles("dev", "test", "prod")
                    .resolve("--profile", "dev,test")
                    .getValues()
        then:
            profiles == ["dev", "test"]
    }

    def "should pass on allowed profile"() {
        when:
            List<String> profiles = profilesResolver()
                    .withAllowedProfiles("dev", "test", "prod")
                    .resolve("--profile", "dev,test")
                    .getValues()
        then:
            profiles == ["dev", "test"]
    }

    def "should fail on exclusive profiles"() {
        when:
            profilesResolver()
                    .withExclusiveProfiles("dev", "test", "prod")
                    .resolve("--profile", "dev,test")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Detected exclusive profiles: [dev, test]"
    }

    def "should pass on non-exclusive profiles"() {
        when:
            List<String> profiles = profilesResolver()
                    .withExclusiveProfiles("dev", "test", "prod")
                    .resolve("--profile", "dev,other")
                    .getValues()
        then:
            profiles == ["dev", "other"]
    }

    def "should return default profiles"() {
        when:
            List<String> profiles = profilesResolver()
                    .withDefaultProfiles("dev")
                    .resolve("--some-param", "abc")
                    .getValues()
        then:
            profiles == ["dev"]
    }

    def "should return profiles from args"() {
        when:
            List<String> profiles = profilesResolver()
                    .withDefaultProfiles("dev")
                    .resolve("--profile", "test")
                    .getValues()
        then:
            profiles == ["test"]
    }

    def "should use custom profile arg name"() {
        when:
            List<String> profiles = profilesResolver()
                    .withProfileArgName("profiles")
                    .resolve("--profiles", "test")
                    .getValues()
        then:
            profiles == ["test"]
    }

    def "should use alias for profile arg name"() {
        when:
            List<String> profiles = profilesResolver()
                    .addArgsAlias("p", "profile")
                    .resolve("-p", "test")
                    .getValues()
        then:
            profiles == ["test"]
    }

    def "should use arg mapping for profile arg name"() {
        when:
            List<String> profiles = profilesResolver()
                    .addArgsMapping(["--prod"] as String[], ["--profile", "prod"] as String[])
                    .resolve("--prod")
                    .getValues()
        then:
            profiles == ["prod"]
    }

    def "should validate profile count"() {
        when:
            List<String> profiles = profilesResolver()
                    .withExpectedProfileCount(2)
                    .resolve("--profile", "dev,test")
                    .getValues()
        then:
            profiles == ["dev", "test"]

        when:
            profilesResolver()
                    .withExpectedProfileCount(3)
                    .resolve("--profile", "dev,test")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Too little active profiles"
    }

    def "should map profiles"() {
        when:
            List<String> profiles = profilesResolver()
                    .withProfilesMapper({ p -> p.collect { it + "-mapped" } })
                    .resolve("--profile", "dev,test")
                    .getValues()
        then:
            profiles == ["dev-mapped", "test-mapped"]
    }
}
