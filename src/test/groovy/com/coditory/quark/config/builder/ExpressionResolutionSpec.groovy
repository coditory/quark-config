package com.coditory.quark.config.builder

import com.coditory.quark.config.Config
import com.coditory.quark.config.UnresolvedConfigExpressionException
import spock.lang.Specification
import spock.lang.Unroll

class ExpressionResolutionSpec extends Specification {
    def "should resolve config with reference to config value"() {
        when:
            Config config = Config.builder()
                    .put("a.b", "\${a.c}")
                    .put("a.c", "C")
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.toMap() == [
                    a: [
                            b: "C",
                            c: "C"
                    ]
            ]
    }

    def "should resolve a reference to a referenced value"() {
        when:
            Config config = Config.builder()
                    .put("a.b", "B")
                    .put("a.c", "\${a.b}")
                    .put("a.d", "\${a.c}")
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.toMap() == [
                    a: [
                            b: "B",
                            c: "B",
                            d: "B"
                    ]
            ]
    }

    def "should resolve a two references in one value"() {
        when:
            Config config = Config.builder()
                    .put("a.b", "B")
                    .put("a.c", "C")
                    .put("a.d", "C-\${a.c}___B-\${a.b}")
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.toMap() == [
                    a: [
                            b: "B",
                            c: "C",
                            d: "C-C___B-B"
                    ]
            ]
    }

    def "should not resolve reference to itself"() {
        when:
            Config config = Config.builder()
                    .put("a.b", "\${a.b}")
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.toMap() == [
                    a: [b: "\${a.b}"]
            ]
    }

    def "should not resolve transitive reference to itself"() {
        when:
            Config config = Config.builder()
                    .put("a.b", "\${a.c}")
                    .put("a.c", "\${a.d}")
                    .put("a.d", "\${a.b}")
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.toMap() == [
                    a: [
                            b: "\${a.c}",
                            c: "\${a.d}",
                            d: "\${a.b}"
                    ]
            ]
    }

    def "should resolve reference to parameter of the same name"() {
        when:
            Config config = Config.builder()
                    .put("x", "\${x}")
                    .resolveExpressions(x: "X")
                    .build()
        then:
            config.toMap() == [x: "X"]
    }

    def "should resolve reference to an object"() {
        when:
            Config config = Config.builder()
                    .put("a.b.c", "ABC")
                    .put("a.d", "\${a.b}")
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.toMap() == [
                    a: [
                            b: [c: "ABC"],
                            d: [c: "ABC"]
                    ]
            ]
    }

    def "should resolve reference to a number"() {
        when:
            Config config = Config.builder()
                    .put("a", "123")
                    .put("b", "\${a}")
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.getInteger("a") == 123
    }

    def "should resolve reference with a default"() {
        when:
            Config config = Config.builder()
                    .put("a", "\${b ? 'A'}")
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.toMap() == [a: "A"]
    }

    @Unroll
    def "should resolve reference with two fallbacks: #variables"() {
        when:
            Config config = Config.builder()
                    .put("a", "\${b ? c ? 'A'}")
                    .resolveExpressionsOrSkip(Config.of(variables))
                    .build()
        then:
            config.toMap() == expected

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
        when:
            Config config = Config.builder()
                    .put("a", expression)
                    .resolveExpressionsOrSkip()
                    .build()
        then:
            config.toMap() == [a: expression]

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
        when:
            Config.builder()
                    .put("a.b", "\${a.b}")
                    .resolveExpressions()
                    .build()
        then:
            UnresolvedConfigExpressionException exception = thrown(UnresolvedConfigExpressionException)
            exception.message == "Unresolved config expression: \${a.b}"
    }

    def "should throw error on an unresolved reference"() {
        when:
            Config.builder()
                    .put("a.b", "\${a.d}")
                    .resolveExpressions()
                    .build()
        then:
            UnresolvedConfigExpressionException exception = thrown(UnresolvedConfigExpressionException)
            exception.message == "Unresolved config expression: \${a.d}"
    }
}
