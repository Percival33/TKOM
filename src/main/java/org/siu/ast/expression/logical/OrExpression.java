package org.siu.ast.expression.logical;

import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@Value
public class OrExpression implements Expression {
    Position position;

    public OrExpression(Expression left, Position position, Expression rightLogicFactor) {
        this.position = position;
    }

    @Override
    public void accept(Visitor visitor) {

    }
}
