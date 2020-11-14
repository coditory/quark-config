package com.coditory.quark.config

import com.coditory.quark.config.base.UsesFiles
import spock.lang.Specification
import spock.lang.Unroll

import static ConfigFactory.configApplicationLoader

class LoadApplicationConfigSpec extends Specification implements UsesFiles {
    def "should load application config from files and arguments"() {
        given:
            writeClasspathFile("application.yml", """
            a: BASE
            b: BASE
            c: BASE
            d: BASE
            """)
            writeClasspathFile("application-prod.yml", """
            b: PROFILE
            c: PROFILE
            d: PROFILE
            """)
            File external = writeFile("external.yml", """
            c: EXTERNAL
            d: EXTERNAL
            """)
        when:
            Config config = stubClassLoader {
                configApplicationLoader()
                        .withArgs(
                                "--profile", "prod",
                                "--config.d", "ARGS",
                                "--config.external", external.getPath())
                        .load()
            }
        then:
            config.toMap() == [
                    a: "BASE",
                    b: "PROFILE",
                    c: "EXTERNAL",
                    d: "ARGS",
            ]
    }

    def "should resolve expressions after all config sources are loaded"() {
        given:
            writeClasspathFile("application.yml", """
            a: BASE
            b: BASE
            c: BASE
            d: BASE
            expression: "\${a}, \${b}, \${c}, \${d}"
            """)
            writeClasspathFile("application-prod.yml", """
            b: PROFILE
            c: PROFILE
            d: PROFILE
            """)
            File external = writeFile("external.yml", """
            c: EXTERNAL
            d: EXTERNAL
            """)
        when:
            Config config = stubClassLoader {
                configApplicationLoader()
                        .withArgs(
                                "--profile", "prod",
                                "--config.d", "ARGS",
                                "--config.external", external.getPath()
                        )
                        .load()
            }
        then:
            config.getString("expression") == "BASE, PROFILE, EXTERNAL, ARGS"
    }

    @Unroll
    def "should use default profile: #profile"() {
        given:
            writeClasspathFile("application.yml", "a: BASE")
            writeClasspathFile("application-prod.yml", "a: PROD")
            writeClasspathFile("application-local.yml", "a: LOCAL")
        when:
            Config config = stubClassLoader {
                configApplicationLoader()
                        .withDefaultProfile(profile)
                        .load()
            }
        then:
            config.getString("a") == value

        where:
            profile | value
            "local" | "LOCAL"
            "prod"  | "PROD"
            "other" | "BASE"
    }

    def "should override default profile with profile argument"() {
        given:
            writeClasspathFile("application.yml", "a: BASE")
            writeClasspathFile("application-prod.yml", "a: PROD")
            writeClasspathFile("application-local.yml", "a: LOCAL\nb: LOCAL")
        when:
            Config config = stubClassLoader {
                configApplicationLoader()
                        .withDefaultProfile("local")
                        .withArgs("--profile", "prod")
                        .load()
            }
        then:
            config.toMap() == [a: "PROD"]
    }

    def "should override default profile with custom profile argument"() {
        given:
            writeClasspathFile("application.yml", "a: BASE")
            writeClasspathFile("application-prod.yml", "a: PROD")
            writeClasspathFile("application-local.yml", "a: LOCAL\nb: LOCAL")
        when:
            Config config = stubClassLoader {
                configApplicationLoader()
                        .withDefaultProfile("local")
                        .withProfileArgument("appprofile")
                        .withArgs("--appprofile", "prod")
                        .load()
            }
        then:
            config.toMap() == [a: "PROD"]
    }

    def "should fail on unresolved expression"() {
        given:
            writeClasspathFile("application.yml", "a: \${x}")
        when:
            stubClassLoader {
                configApplicationLoader()
                        .load()
            }
        then:
            UnresolvedConfigExpressionException e = thrown(UnresolvedConfigExpressionException)
            e.message == "Unresolved config expression: \${x}"
    }
}
