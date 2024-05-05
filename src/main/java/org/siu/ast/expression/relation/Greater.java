package org.siu.ast.expression.relation;

import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@Value
public class Greater implements Expression {
    Position position;
    @Override
    public void accept(Visitor visitor) {

    }
}
