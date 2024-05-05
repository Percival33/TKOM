package org.siu.ast.expression.logical;

import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@Value
public class AndExpression implements Expression {
    Position position;

    @Override
    public void accept(Visitor visitor) {

    }
}