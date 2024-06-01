package org.siu.ast.expression.arithmetic;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@ToString(exclude = {"left", "right"})
@EqualsAndHashCode(exclude="position")
@Value
public class DivideArithmeticExpression implements BinaryArithmeticExpression {
    Expression left;
    Expression right;

    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int evaluate(int left, int right) {
        return left / right;
    }

    @Override
    public float evaluate(float left, float right) {
        return left / right;
    }
}
