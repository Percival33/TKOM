package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.siu.ast.Statement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.List;

@EqualsAndHashCode(exclude = "position")
@Value
@RequiredArgsConstructor
public class FunctionCallExpression implements Expression, Statement {
    String identifier;
    List<Expression> arguments;
    Position position;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
