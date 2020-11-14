package com.coditory.config;

import java.util.Objects;

class ExpressionResolver {
    private final Config config;

    public ExpressionResolver(Config config) {
        this.config = Objects.requireNonNull(config);
    }

    public Object resolve(Object value) {
        if (value instanceof Expression) {
            return resolve((Expression) value);
        }
        return value;
    }

    private Object resolve(Expression expression) {
        return expression.resolve(config);
    }
}
