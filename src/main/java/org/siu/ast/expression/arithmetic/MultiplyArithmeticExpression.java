package org.siu.ast.expression.arithmetic;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@ToString(exclude = {"left", "right"})
@EqualsAndHashCode(exclude = "position")
@Value
public class MultiplyArithmeticExpression implements BinaryArithmeticExpression {
    Expression left;
    Expression right;

    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int evaluate(int left, int right) {
        if (left > Integer.MAX_VALUE / right) {
            throw new ArithmeticException("Integer overflow at " + position.toString());
        }
        return left * right;
    }

    @Override
    public float evaluate(float left, float right) {
        if (left > Float.MAX_VALUE / right) {
            throw new ArithmeticException("Float overflow at " + position.toString());
        }
        return left * right;
    }
}
