package com.coditory.quark.config

import com.coditory.quark.config.base.UsesFiles
import spock.lang.Specification
import spock.lang.Unroll

import static ConfigFactory.configLoader

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
                configLoader()
                        .withArgs(
                                "--profile", "prod",
                                "--config-prop.d", "ARGS",
                                "--config", external.getPath()
                        )
                        .loadConfig()
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
                configLoader()
                        .withArgs(
                                "--profile", "prod",
                                "--config-prop.d", "ARGS",
                                "--config", external.getPath()
                        )
                        .loadConfig()
            }
        then:
            config.getString("expression") == "BASE, PROFILE, EXTERNAL, ARGS"
    }

    @Unroll
    def "should use value from default profile: #profiles"() {
        given:
            writeClasspathFile("application.yml", "a: BASE")
            writeClasspathFile("application-prod.yml", "a: PROD")
            writeClasspathFile("application-local.yml", "a: LOCAL")
        when:
            Config config = stubClassLoader {
                configLoader()
                        .withDefaultProfiles(*profiles)
                        .loadConfig()
            }
        then:
            config.getString("a") == value

        where:
            profiles             | value
            ["local"]            | "LOCAL"
            ["prod"]             | "PROD"
            ["local", "prod"]    | "PROD"
            ["prod", "local"]    | "LOCAL"
            ["local", "unknown"] | "LOCAL"
    }

    def "should load environment"() {
        given:
            writeClasspathFile("application.yml", "a: BASE")
            writeClasspathFile("application-local.yml", "a: LOCAL")
        when:
            Environment env = stubClassLoader {
                configLoader()
                        .withDefaultProfiles("local")
                        .loadEnvironment()
            }
        then:
            env.profiles == Profiles.of("local")
            env.config == Config.of(a: "LOCAL")
    }

    def "should use profile specified in loader"() {
        given:
            writeClasspathFile("application.yml", "a: BASE")
            writeClasspathFile("application-local.yml", "a: LOCAL")
        when:
            Config config = stubClassLoader {
                configLoader()
                        .withDefaultProfiles("test")
                        .withProfiles("local")
                        .withArgs("--profile", "dev")
                        .loadConfig()
            }
        then:
            config.getString("a") == "LOCAL"
    }

    @Unroll
    def "should throw error on missing profile config file: #profiles"() {
        given:
            writeClasspathFile("application.yml", "a: BASE")
            writeClasspathFile("application-local.yml", "a: LOCAL")

        when:
            stubClassLoader {
                configLoader()
                        .withProfileConfigsRequired()
                        .withDefaultProfiles(*profiles)
                        .loadConfig()
            }
        then:
            thrown(ConfigLoadException)

        where:
            profiles << [
                    ["other"],
                    ["local", "other"],
                    ["other", "local"]
            ]
    }

    def "should load config from custom path and with custom name"() {
        given:
            writeClasspathFile("configs/app.yml", "a: BASE")
            writeClasspathFile("configs/app-local.yml", "b: LOCAL")

        when:
            Config config = stubClassLoader {
                configLoader()
                        .withConfigPath("configs")
                        .withConfigBaseName("app")
                        .withDefaultProfiles("local")
                        .loadConfig()
            }
        then:
            config.getString("a") == "BASE"
            config.getString("b") == "LOCAL"
    }

    def "should load config from custom path and without a base name"() {
        given:
            writeClasspathFile("configs/common.yml", "a: COMMON")
            writeClasspathFile("configs/local.yml", "b: LOCAL")

        when:
            Config config = stubClassLoader {
                configLoader()
                        .withConfigPath("configs")
                        .withoutConfigBaseName()
                        .withCommonConfigName("common")
                        .withDefaultProfiles("local")
                        .loadConfig()
            }
        then:
            config.getString("a") == "COMMON"
            config.getString("b") == "LOCAL"
    }

    def "should override default profile with profile argument"() {
        given:
            writeClasspathFile("application.yml", "a: BASE")
            writeClasspathFile("application-prod.yml", "a: PROD")
            writeClasspathFile("application-local.yml", "a: LOCAL\nb: LOCAL")
        when:
            Config config = stubClassLoader {
                configLoader()
                        .withDefaultProfiles("local")
                        .withArgs("--profile", "prod")
                        .loadConfig()
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
                configLoader()
                        .withDefaultProfiles("local")
                        .withProfileArgName("appprofile")
                        .withArgs("--appprofile", "prod")
                        .loadConfig()
            }
        then:
            config.toMap() == [a: "PROD"]
    }

    def "should fail on unresolved expression"() {
        given:
            writeClasspathFile("application.yml", "a: \${x}")
        when:
            stubClassLoader {
                configLoader()
                        .loadConfig()
            }
        then:
            UnresolvedConfigExpressionException e = thrown(UnresolvedConfigExpressionException)
            e.message == "Unresolved config expression: \${x}"
    }

    def "should skip missing optional profile configs"() {
        given:
            writeClasspathFile("application.yml", "a: A")
        expect:
            stubClassLoader {
                configLoader()
                        .withArgs("--profile", "other")
                        .loadConfig()
            }
        and:
            stubClassLoader {
                configLoader()
                        .withProfileConfigsRequired()
                        .withOptionalProfileConfigs("other")
                        .withArgs("--profile", "other")
                        .loadConfig()
            }
    }

    def "should throw error on missing required profile config"() {
        given:
            writeClasspathFile("application.yml", "a: A")
        when:
            stubClassLoader {
                configLoader()
                        .withOptionalProfileConfigs("other")
                        .withArgs("--profile", "prod")
                        .loadConfig()
            }
        then:
            ConfigLoadException e = thrown(ConfigLoadException)
            e.message == "Configuration file not found on classpath: application-prod"
        when:
            stubClassLoader {
                configLoader()
                        .withProfileConfigsRequired()
                        .withArgs("--profile", "prod")
                        .loadConfig()
            }
        then:
            e = thrown(ConfigLoadException)
            e.message == "Configuration file not found on classpath: application-prod"
    }

    def "should make base config optional"() {
        when:
            Config config = stubClassLoader {
                configLoader()
                        .withOptionalBaseConfig()
                        .loadConfig()
            }
        then:
            config.isEmpty()
    }
}
