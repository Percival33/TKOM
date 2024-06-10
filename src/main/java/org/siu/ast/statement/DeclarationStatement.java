package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Parameter;
import org.siu.ast.expression.Expression;
import org.siu.ast.Statement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;


@ToString(exclude = {"expression"})
@EqualsAndHashCode(exclude = "position")
@Value
public class DeclarationStatement implements NamedStatement, Statement {
    Parameter parameter;
    Expression expression;
    Position position;

    @Override
    public void accept(Visitor visitor) {
         visitor.visit(this);
    }

    @Override
    public String getName() {
        return parameter.getName();
    }
}
