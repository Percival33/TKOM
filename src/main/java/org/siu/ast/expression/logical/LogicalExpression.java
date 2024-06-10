package org.siu.ast.expression.logical;

import org.siu.ast.expression.Expression;
import org.siu.token.Position;

public interface LogicalExpression extends Expression {
    Expression getLeft();
    Expression getRight();
    Position getPosition();
    boolean evaluate(boolean left, boolean right);
}
