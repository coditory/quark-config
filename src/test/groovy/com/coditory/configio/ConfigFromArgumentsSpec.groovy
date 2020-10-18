package com.coditory.configio

import spock.lang.Specification
import spock.lang.Unroll

class ConfigFromArgumentsSpec extends Specification {
    @Unroll
    def "should create empty config for args: #args"() {
        when:
            Config config = Config.fromArgs(*args)
        then:
            config.isEmpty()
        where:
            args << [
                    [], [""], ["a"], ["a", "b"]
            ]
    }
    @Unroll
    def "should create config for boolean flags: #args"() {
        when:
            Config config = Config.fromArgs(["a": "flagA", "b": "flagB"], *args)
        then:
            config.toMap() == values
        where:
            args                     | values
            ["--paramA"]             | ["paramA": true]
            ["--paramA", "--paramB"] | ["paramA": true, "paramB": true]
            ["-a", "-c"]             | ["flagA": true]
            ["-a", "-b"]             | ["flagA": true, "flagB": true]
    }

    def "should skip some args that are not flags nor values"() {
        when:
            Config config = Config.fromArgs("x", "--prod", "--port", "8080", "x", "--threads", "100", "x")
        then:
            config.toMap() == [
                    "prod"   : true,
                    "port"   : "8080",
                    "threads": "100"
            ]
    }

    def "should undefined aliases"() {
        when:
            Config config = Config.fromArgs("-p", "--port", "8080", "-t", "100", "x")
        then:
            config.toMap() == [
                    "port": "8080"
            ]
    }

    def "should handle values defined with '='"() {
        when:
            Config config = Config.fromArgs(["t": "threads"], "--port=8080", "other", "-t=100")
        then:
            config.toMap() == [
                    "port": "8080",
                    "threads": "100"
            ]
    }

    def "should parse values from arguments"() {
        given:
            Config config = Config.fromArgs(["t": "threads"], "--port=8080", "other", "-t=100", "--env", "prod")
        expect:
            config.getInteger("port") == 8080
        and:
            config.getInteger("threads") == 100
        and:
            config.getString("env") == "prod"
    }
}
