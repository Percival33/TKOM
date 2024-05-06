package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.type.ValueType;
import org.siu.parser.Visitor;
import org.siu.token.Position;

@ToString(exclude = "expression")
@EqualsAndHashCode(exclude = "position")
@RequiredArgsConstructor
@Value
public class CastedFactorExpression implements Expression {
    ValueType type;
    Expression expression;
    Position position;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
