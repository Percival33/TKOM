package org.siu.ast.expression.logical;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.ast.expression.LogicalExpression;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@ToString(exclude = {"left", "right"})
@EqualsAndHashCode(exclude = "position")
@Value
public class AndLogicalExpression implements LogicalExpression {
    Expression left;
    Expression right;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean evaluate(boolean left, boolean right) {
        return left && right;
    }
}
