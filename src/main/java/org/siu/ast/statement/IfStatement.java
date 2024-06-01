package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.BlockStatement;
import org.siu.ast.Statement;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

import java.util.List;
import java.util.Optional;

@ToString(exclude = {"conditions", "ifInstructions", "elseInstructions"})
@EqualsAndHashCode(exclude = "position")
@Value
public class IfStatement implements Statement {
    List<Expression> conditions;
    List<BlockStatement> ifInstructions;
    Optional<BlockStatement> elseInstructions;

    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
