package org.siu.ast.expression.logical;

import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@Value
public class NegateLogicalExpression implements Expression {
    Expression expression;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
