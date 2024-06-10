package org.siu.ast.expression;

public interface NamedExpression extends Expression {
    default String getName() {
        throw new UnsupportedOperationException();
    }
}
