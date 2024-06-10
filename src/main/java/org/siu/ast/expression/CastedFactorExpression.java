package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.type.TypeDeclaration;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@ToString(exclude = "expression")
@EqualsAndHashCode(exclude = "position")
@RequiredArgsConstructor
@Value
public class CastedFactorExpression implements Expression {
    TypeDeclaration type;
    Expression expression;
    Position position;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
