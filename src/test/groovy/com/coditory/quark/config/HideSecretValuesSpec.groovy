package com.coditory.quark.config

import spock.lang.Specification
import spock.lang.Unroll

class HideSecretValuesSpec extends Specification {
    @Unroll
    def "should hide secrets under field name: #field"() {
        given:
            Config config = Config.builder()
                    .put("a.${field}", "SECRET")
                    .put("a.${field}.c", "SECRET")
                    .put("b.${field}[0]", "SECRET")
                    .put("b.${field}[1]", "SECRET")
                    .put("c.${field}[0].x", "SECRET")
                    .put("c.${field}[1][0]", "SECRET")
                    .put("c.${field}.x.[0].x", "SECRET")
                    .put("c.${field}.y.[0][0]", "SECRET")
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
                    .put("a.password", "SECRET")
                    .put("a.secret.c", "SECRET")
                    .put("a.token.x[0]", "SECRET")
                    .put("a.token.x[1]", "SECRET")
                    .put("b.x", "NOT-SECRET")
                    .put("b.pas.sword", "NOT-SECRET")
                    .put("b.x.c", "NOT-SECRET")
                    .put("b.x.[0]", "NOT-SECRET")
                    .put("b.x.[1]", "NOT-SECRET")
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
