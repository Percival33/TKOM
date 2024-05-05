package org.siu.ast.type;

import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@Value
public class FloatExpression implements Expression {
    float value;
    Position position;
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
