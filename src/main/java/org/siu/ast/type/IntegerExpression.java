package org.siu.ast.type;

import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@Value
public class IntegerExpression implements Expression {
    int value;
    Position position;
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
