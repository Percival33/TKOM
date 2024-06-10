package org.siu.ast.expression.relation;

import org.siu.ast.expression.Expression;

public interface RelationExpression extends Expression {
    Expression getLeft();
    Expression getRight();

    boolean evaluate(int left, int right);
    boolean evaluate(float left, float right);
}
