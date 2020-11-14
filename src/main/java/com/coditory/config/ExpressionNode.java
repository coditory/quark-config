package com.coditory.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.coditory.config.ExpressionNode.staticNode;

interface ExpressionNode {
    static ExpressionNode staticNode(Object value) {
        return new ExpressionStaticNode(value);
    }

    static ExpressionNode expressionNode(String expression, List<String> alternativeValues) {
        return new ExpressionDynamicNode(expression, alternativeValues);
    }

    boolean isStatic();

    Object getValue();

    ExpressionNode resolve(Config config, Set<Expression> visited);
}

class ExpressionStaticNode implements ExpressionNode {
    private final Object value;

    public ExpressionStaticNode(Object value) {
        this.value = value;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public ExpressionNode resolve(Config config, Set<Expression> visited) {
        return this;
    }
}

class ExpressionDynamicNode implements ExpressionNode {
    private final List<String> alternativeValues;
    private final String expression;

    public ExpressionDynamicNode(String expression, List<String> alternativeValues) {
        this.alternativeValues = List.copyOf(alternativeValues);
        this.expression = expression;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public Object getValue() {
        return expression;
    }

    @Override
    public ExpressionNode resolve(Config config, Set<Expression> visited) {
        Object result = null;
        for (int i = 0; i < alternativeValues.size() && result == null; ++i) {
            String alternativeValue = alternativeValues.get(i);
            result = resolveReference(config, visited, alternativeValue);
        }
        return result != null
                ? staticNode(result)
                : defaultValue();
    }

    private Object resolveReference(Config config, Set<Expression> visited, String expressionPath) {
        Object value = config.getObjectOrNull(expressionPath);
        if (value instanceof Expression && !visited.contains(value)) {
            Expression expression = (Expression) value;
            Set<Expression> resolving = new HashSet<>(visited);
            resolving.add(expression);
            value = expression.resolve(config, resolving);
        }
        return value instanceof Expression ? null : value;
    }

    private ExpressionNode defaultValue() {
        return alternativeValues.size() == 1
                ? this
                : staticNode(alternativeValues.get(alternativeValues.size() - 1));
    }
}