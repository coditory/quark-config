package com.coditory.quark.config

import spock.lang.Specification

class HideSecretValuesSpec extends Specification {
    def "should hide secret values with field name: #field"() {
        given:
            String path = "a.${field}"
            Config config = Config.builder()
                    .put(path, "SECRET")
                    .build()
        when:
            Config result = config.withHiddenSecrets()
        then:
            result.getString(path) == "***"
        where:
            field << [
                    "secret", "secrets", "password", "passwords", "token", "tokens", "key", "keys", "apiKey", "clientSecret"
            ]
    }

    def "should hide secret value on path: #path"() {
        given:
            Config config = Config.builder()
                    .put(path, "SECRET")
                    .build()
        when:
            Config result = config.withHiddenSecrets()
        then:
            result.getString(path) == "***"
        where:
            path << [
                    "b.secret", "b.token.secret[0]", "b.secretSth", "b.sthSecret", "b.sth-secret", "b.secret-sth"
            ]
    }

    def "should not hide non secret values"() {
        given:
            Config config = Config.builder()
                    .put(path, "NOT-SECRET")
                    .build()
        when:
            Config result = config.withHiddenSecrets()
        then:
            result.getString(path) == "NOT-SECRET"
        where:
            path << [
                    "b.secret.c", "b.token.x[0]", "b.x", "b.pas.sword", "nontoken"
            ]
    }
}
