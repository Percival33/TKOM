package org.siu.ast.expression.arithmetic;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.expression.ArithmeticExpression;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@ToString(exclude = {"left", "right"})
@EqualsAndHashCode(exclude="position")
@Value
public class MultiplyArithmeticExpression implements ArithmeticExpression {
    Expression left;
    Expression right;

    Position position;

    @Override
    public void accept(Visitor visitor) {}

    @Override
    public int evaluate(int left, int right) {
        return left * right;
    }
}
