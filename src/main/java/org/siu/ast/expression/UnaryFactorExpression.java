package org.siu.ast.expression;

import lombok.Value;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@Value
public class UnaryFactorExpression implements Expression {
    Expression expression;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
