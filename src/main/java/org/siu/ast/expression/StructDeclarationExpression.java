package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.List;

@ToString(exclude = {"arguments"})
@EqualsAndHashCode(exclude = "position")
@Value
public class StructDeclarationExpression implements Expression {
    List<Expression> arguments;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
