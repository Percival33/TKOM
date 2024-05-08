package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.ast.Statement;
import org.siu.parser.Visitor;
import org.siu.token.Position;

import java.util.Optional;

@ToString(exclude = {"expression"})
@EqualsAndHashCode(exclude = "position")
@Value
public class ReturnStatement implements Statement {
    Expression expression;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
