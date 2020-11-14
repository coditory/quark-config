package com.coditory.quark.config

import spock.lang.Specification
import spock.lang.Unroll

class HideSecretValuesSpec extends Specification {
    @Unroll
    def "should hide secrets under field name: #field"() {
        given:
            Config config = Config.builder()
                    .withValue("a.${field}", "SECRET")
                    .withValue("a.${field}.c", "SECRET")
                    .withValue("b.${field}[0]", "SECRET")
                    .withValue("b.${field}[1]", "SECRET")
                    .withValue("c.${field}[0].x", "SECRET")
                    .withValue("c.${field}[1][0]", "SECRET")
                    .withValue("c.${field}.x.[0].x", "SECRET")
                    .withValue("c.${field}.y.[0][0]", "SECRET")
                    .build()
        when:
            Config result = config.withHiddenSecrets()
        then:
            result.toMap() == [
                    a: [(field): [c: "***"]],
                    b: [(field): ["***", "***"]],
                    c: [(field): [
                            x: [[x: "***"]],
                            y: [["***"]]]
                    ]
            ]
        where:
            field << [
                    "secret", "secrets", "password", "passwords", "token", "tokens"
            ]
    }

    def "should not hide non secret values"() {
        given:
            Config config = Config.builder()
                    .withValue("a.password", "SECRET")
                    .withValue("a.secret.c", "SECRET")
                    .withValue("a.token.x[0]", "SECRET")
                    .withValue("a.token.x[1]", "SECRET")
                    .withValue("b.x", "NOT-SECRET")
                    .withValue("b.pas.sword", "NOT-SECRET")
                    .withValue("b.x.c", "NOT-SECRET")
                    .withValue("b.x.[0]", "NOT-SECRET")
                    .withValue("b.x.[1]", "NOT-SECRET")
                    .build()
        when:
            Config result = config.withHiddenSecrets()
        then:
            result.toMap() == [
                    a: [
                            password: "***",
                            secret  : [c: "***"],
                            token   : [x: ["***", "***"]]
                    ],
                    b: [
                            x  : ["NOT-SECRET", "NOT-SECRET"],
                            pas: [sword: "NOT-SECRET"]
                    ]
            ]
    }
}
