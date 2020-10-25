package com.coditory.configio


import com.coditory.configio.base.UsesFiles
import spock.lang.Specification

import static com.coditory.configio.ApplicationConfigLoader.applicationConfigLoader

class ApplicationConfigLoaderSpec extends Specification implements UsesFiles {
    def "should add values to an empty config"() {
        given:
            writeClasspathFile("application.yml", """
            |a: BASE
            |b: BASE
            |c: BASE
            |d: BASE
            """)
            writeClasspathFile("application-prod.yml", """
            |b: PROFILE
            |c: PROFILE
            |d: PROFILE
            """)
            File external = writeFile("external.yml", """
            |c: EXTERNAL
            |d: EXTERNAL
            """)
        when:
            Config config = stubClassLoader {
                applicationConfigLoader()
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
}
