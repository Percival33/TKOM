package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.BlockStatement;
import org.siu.ast.Statement;
import org.siu.ast.expression.Expression;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@ToString(exclude = {"condition", "block"})
@EqualsAndHashCode(exclude = "position")
@Value
public class WhileStatement implements Statement {
    Expression condition;
    BlockStatement block;

    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
