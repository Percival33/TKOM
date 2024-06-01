package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@EqualsAndHashCode(exclude = "position")
@Value
@RequiredArgsConstructor
public class IdentifierExpression implements Expression {
    String identifier;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
