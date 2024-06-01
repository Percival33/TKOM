package org.siu.ast.expression;

public interface RelationExpression extends Expression {
    Expression getLeft();
    Expression getRight();

    boolean evaluate(int left, int right);
}
