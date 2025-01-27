package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@EqualsAndHashCode(exclude = "position")
@Value
public class VariantDeclarationExpression implements Expression {
    String typeName;
    String fieldName;
    Expression expression;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
