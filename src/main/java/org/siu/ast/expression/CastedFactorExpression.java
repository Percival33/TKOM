package org.siu.ast.expression;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.siu.ast.type.ValueType;
import org.siu.parser.Visitor;
import org.siu.token.Position;

import java.util.List;

@Value
@RequiredArgsConstructor
public class CastedFactorExpression implements Expression {
    ValueType type;
    Expression expression;
    Position position;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
