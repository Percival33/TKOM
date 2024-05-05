package org.siu.ast.expression.logical;

import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

public class OrExpression implements Expression {
    public OrExpression(Expression left, Position position, Expression rightLogicFactor) {
    }

    @Override
    public void accept(Visitor visitor) {

    }
}
