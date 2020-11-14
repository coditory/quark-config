package com.coditory.quark.config

import spock.lang.Specification
import spock.lang.Unroll

class ExpressionResolutionSpec extends Specification {
    def "should resolve config with reference to config value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "\${a.c}")
                    .withValue("a.c", "C")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.toMap() == [
                    a: [
                            b: "C",
                            c: "C"
                    ]
            ]
    }

    def "should resolve a reference to a referenced value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "B")
                    .withValue("a.c", "\${a.b}")
                    .withValue("a.d", "\${a.c}")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.toMap() == [
                    a: [
                            b: "B",
                            c: "B",
                            d: "B"
                    ]
            ]
    }

    def "should resolve a two references in one value"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "B")
                    .withValue("a.c", "C")
                    .withValue("a.d", "C-\${a.c}___B-\${a.b}")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.toMap() == [
                    a: [
                            b: "B",
                            c: "C",
                            d: "C-C___B-B"
                    ]
            ]
    }

    def "should not resolve reference to itself"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "\${a.b}")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.toMap() == [
                    a: [b: "\${a.b}"]
            ]
    }

    def "should not resolve transitive reference to itself"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "\${a.c}")
                    .withValue("a.c", "\${a.d}")
                    .withValue("a.d", "\${a.b}")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.toMap() == [
                    a: [
                            b: "\${a.c}",
                            c: "\${a.d}",
                            d: "\${a.b}"
                    ]
            ]
    }

    def "should resolve reference to an object"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b.c", "ABC")
                    .withValue("a.d", "\${a.b}")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.toMap() == [
                    a: [
                            b: [c: "ABC"],
                            d: [c: "ABC"]
                    ]
            ]
    }

    def "should resolve reference to a number"() {
        given:
            Config config = Config.builder()
                    .withValue("a", "123")
                    .withValue("b", "\${a}")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.getInteger("a") == 123
    }

    def "should resolve reference with a default"() {
        given:
            Config config = Config.builder()
                    .withValue("a", "\${b ? 'A'}")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.toMap() == [a: "A"]
    }

    @Unroll
    def "should resolve reference with two fallbacks: #variables"() {
        given:
            Config config = Config.builder()
                    .withValue("a", "\${b ? c ? 'A'}")
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip(Config.of(variables))
        then:
            resolved.toMap() == expected

        where:
            variables            || expected
            [b: "B"]             || [a: "B"]
            [c: "C"]             || [a: "C"]
            [:]                  || [a: "A"]
            [b: "B", c: "C"]     || [a: "B"]
            [c: "\${b}"]         || [a: "A"]
            [b: "\${a}"]         || [a: "A"]
            [b: "\${c}"]         || [a: "A"]
            [b: "\${c}", c: "C"] || [a: "C"]
    }

    @Unroll
    def "should not modify incorrect expression #expression"() {
        given:
            Config config = Config.builder()
                    .withValue("a", expression)
                    .build()
        when:
            Config resolved = config.resolveExpressionsOrSkip()
        then:
            resolved.toMap() == [a: expression]

        where:
            expression << [
                    "a}",
                    "{a}",
                    "}}}",
                    "{{{",
                    "{}",
                    "\${a"
            ]
    }

    def "should throw error on a reference to itself"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "\${a.b}")
                    .build()
        when:
            config.resolveExpressions()
        then:
            UnresolvedConfigExpressionException exception = thrown(UnresolvedConfigExpressionException)
            exception.message == "Unresolved config expression: \${a.b}"
    }

    def "should throw error on an unresolved reference"() {
        given:
            Config config = Config.builder()
                    .withValue("a.b", "\${a.d}")
                    .build()
        when:
            config.resolveExpressions()
        then:
            UnresolvedConfigExpressionException exception = thrown(UnresolvedConfigExpressionException)
            exception.message == "Unresolved config expression: \${a.d}"
    }
}
