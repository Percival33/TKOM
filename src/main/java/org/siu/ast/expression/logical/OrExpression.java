package org.siu.ast.expression.logical;

import org.siu.ast.expression.Expression;
import org.siu.ast.expression.Expression2;
import org.siu.parser.Visitor;
import org.siu.token.Position;

public class OrExpression extends Expression2 {
    public OrExpression(Expression left, Position position, Expression rightLogicFactor) {
        super();
    }

    @Override
    public void accept(Visitor visitor) {

    }
}
