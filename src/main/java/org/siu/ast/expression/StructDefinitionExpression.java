package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.List;

@ToString(exclude = {"parameters"})
@EqualsAndHashCode(exclude = "position")
@Value
public class StructDefinitionExpression implements Expression {
    List<Expression> arguments;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
