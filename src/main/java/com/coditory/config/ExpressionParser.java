package com.coditory.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.coditory.config.ExpressionNode.staticNode;
import static java.util.stream.Collectors.toList;

class ExpressionParser {
    static Object parse(Object template) {
        if (!(template instanceof String)) {
            return template;
        }
        Expression expression = parse((String) template);
        return expression.isStatic()
                ? template
                : expression;
    }

    private static Expression parse(String template) {
        List<ExpressionNode> nodes = new ArrayList<>();
        boolean expression = false;
        Set<Character> escapable = Set.of('{', '}');
        boolean escaped = false;
        StringBuilder chunk = new StringBuilder();
        char[] chars = template.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (escaped) {
                if (!escapable.contains(c)) {
                    throw new RuntimeException("Could not escape: \"" + c + "\" in template: \"" + template + "\"");
                }
                escaped = false;
                chunk.append(c);
            } else if ('\\' == c) {
                escaped = true;
            } else if (startsWith(chars, i, "${")) {
                if (chunk.length() > 0) {
                    nodes.add(staticNode(chunk.toString()));
                    chunk = new StringBuilder();
                }
                expression = true;
                i += 1;
            } else if ('}' == c && expression) {
                if (chunk.length() > 0) {
                    nodes.add(parseExpression(chunk.toString().trim()));
                    chunk = new StringBuilder();
                }
                expression = false;
            } else {
                chunk.append(c);
            }
        }
        if (chunk.length() > 0) {
            nodes.add(staticNode(chunk.toString()));
        }
        return new Expression(template, nodes);
    }

    private static ExpressionNode parseExpression(String expression) {
        List<String> chunks = QuotedSpliterator.splitBy(expression, "?:").stream()
                .map(String::trim)
                .filter(it -> !it.isBlank())
                .collect(toList());
        if (chunks.isEmpty()) {
            return staticNode("");
        }
        return ExpressionNode.expressionNode(expression, chunks);
    }

    private static boolean startsWith(char[] c, int index, String value) {
        if (c.length < value.length() + index) {
            return false;
        }
        boolean match = true;
        int i = 0;
        while (i < value.length() && match) {
            match = value.charAt(i) == c[index + i];
            ++i;
        }
        return match && i == value.length();
    }
}
