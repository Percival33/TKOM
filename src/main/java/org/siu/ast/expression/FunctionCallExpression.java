package org.siu.ast.expression;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.siu.parser.Visitor;
import org.siu.token.Position;

import java.util.List;

@Value
@RequiredArgsConstructor
public class FunctionCallExpression implements Expression {
    String identifier;
    List<Expression> arguments;
    Position position;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
