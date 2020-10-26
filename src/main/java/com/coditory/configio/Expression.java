package com.coditory.configio;

import com.coditory.configio.api.UnresolvedConfigExpression;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class Expression {
    static Object unwrap(Object value) {
        return value instanceof Expression
                ? ((Expression) value).getExpression()
                : value;
    }

    static Object failOnUnresolved(Object value) {
        if (value instanceof Expression) {
            Expression expression = (Expression) value;
            throw new UnresolvedConfigExpression("Unresolved config expression: " + expression.getExpression());
        }
        return value;
    }

    private final String expression;
    private final List<ExpressionNode> expressionNodes;

    Expression(String expression, List<ExpressionNode> expressionNodes) {
        this.expression = requireNonNull(expression);
        this.expressionNodes = requireNonNull(expressionNodes);
    }

    public String getExpression() {
        return expression;
    }

    public boolean isStatic() {
        return expressionNodes.stream()
                .allMatch(ExpressionNode::isStatic);
    }

    public Object resolve(Config config) {
        return resolve(config, Set.of());
    }

    public Object resolve(Config config, Set<Expression> visited) {
        List<ExpressionNode> nodes = expressionNodes.stream()
                .map(node -> node.resolve(config, visited))
                .collect(toList());
        if (Objects.equals(nodes, this.expressionNodes)) {
            return this;
        }
        boolean resolved = nodes.stream()
                .allMatch(ExpressionNode::isStatic);
        return resolved
                ? resolveStaticNodes(nodes)
                : new Expression(expression, nodes);
    }

    private Object resolveStaticNodes(List<ExpressionNode> nodes) {
        if (nodes.size() == 1) {
            return nodes.get(0).getValue();
        }
        return nodes.stream()
                .filter(ExpressionNode::isStatic)
                .map(ExpressionNode::getValue)
                .map(Objects::toString)
                .collect(joining());
    }

    @Override
    public String toString() {
        return "Expression{" + expression + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression that = (Expression) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }
}
