package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@EqualsAndHashCode(exclude = "position")
@Value
public class VariantExpression implements Expression {
    String fieldName;
    Expression expression;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
