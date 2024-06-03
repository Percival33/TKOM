package org.siu.ast.expression;

import org.siu.token.Position;

public interface LogicalExpression extends Expression {
    Expression getLeft();
    Expression getRight();
    Position getPosition();
    boolean evaluate(boolean left, boolean right);
}
