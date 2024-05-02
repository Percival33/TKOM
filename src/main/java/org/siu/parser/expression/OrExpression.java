package org.siu.parser.expression;

import org.siu.parser.Visitor;
import org.siu.token.Position;

public class OrExpression extends Expression {
    public OrExpression(Expression left, Position position, Expression rightLogicFactor) {
        super();
    }

    @Override
    public void accept(Visitor visitor) {

    }
}
