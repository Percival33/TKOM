package org.siu.ast.type;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@EqualsAndHashCode(exclude = "position")
@Value
public class IntegerExpression implements Expression {
    int value;
    Position position;
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
