package org.siu.ast.expression.arithmetic;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.expression.ArithmeticExpression;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@ToString(exclude = "expression")
@EqualsAndHashCode(exclude="position")
@Value
public class NegateArithmeticExpression implements ArithmeticExpression {
    Expression expression;

    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
