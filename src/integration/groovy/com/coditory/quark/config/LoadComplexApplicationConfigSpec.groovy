package com.coditory.quark.config

import com.coditory.quark.config.base.UsesFiles
import spock.lang.Specification
import spock.lang.Unroll

class LoadComplexApplicationConfigSpec extends Specification implements UsesFiles {
    @Unroll
    def "should load config with advanced setup for args: #args"() {
        given:
            writeClasspathFile("config/application.yml", "profiles: \${_profiles}\nbase: true\nvalue: BASE")
            writeClasspathFile("config/application-local.yml", "local: true\nvalue: LOCAL")
            writeClasspathFile("config/application-dev.yml", "dev: true\nvalue: DEV")
            writeClasspathFile("config/application-test.yml", "test: true\nvalue: TEST")
            writeClasspathFile("config/application-prod.yml", "prod: true\nvalue: PROD")
            writeClasspathFile("config/application-other.yml", "other: true\nvalue: OTHER")
        when:
            Config config = stubClassLoader { load(args) }
        then:
            config.toMap() == expected
        where:
            args                       || expected
            []                         || [profiles: ["local"], base: true, local: true, value: "LOCAL"]
            ["--profile", "local"]     || [profiles: ["local"], base: true, local: true, value: "LOCAL"]
            ["--profile", "prod"]      || [profiles: ["prod"], base: true, prod: true, value: "PROD"]
            ["--profile", "dev"]       || [profiles: ["dev"], base: true, dev: true, value: "DEV"]
            ["-p", "dev"]              || [profiles: ["dev"], base: true, dev: true, value: "DEV"]
            ["--test"]                 || [profiles: ["test"], base: true, test: true, value: "TEST"]
            ["--profile", "dev,other"] || [profiles: ["dev", "other"], base: true, dev: true, other: true, value: "OTHER"]
            ["--profile", "other,dev"] || [profiles: ["other", "dev"], base: true, dev: true, other: true, value: "DEV"]
            ["--profile", "other"]     || [profiles: ["local", "other"], base: true, local: true, other: true, value: "OTHER"]
    }

    private Config load(List<String> args) {
        List<String> mainProfiles = List.of("local", "dev", "test", "prod")
        return new ConfigLoader()
                .addArgsMapping(["--prod"], ["--profile", "prod"])
                .addArgsMapping(["--test"], ["--profile", "test"])
                .addArgsMapping(["--dev"], ["--profile", "dev"])
                .addArgsAlias("p", "profile")
                .withMinProfileCount(1)
                .withExclusiveProfiles(mainProfiles)
                .setFirstIfNoneMatch(mainProfiles)
                .withConfigPath("config")
                .withArgs(args)
                .loadConfig()
    }
}
