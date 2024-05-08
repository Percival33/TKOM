package org.siu.ast.expression;

import lombok.Value;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@Value
public class CopiedValueExpression implements Expression {
    Expression expression;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
