package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@Value
@EqualsAndHashCode(exclude = "position")
@RequiredArgsConstructor
public class StructExpression implements Expression {
    String structName;
    String fieldName;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
