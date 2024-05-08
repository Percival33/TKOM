package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Statement;
import org.siu.ast.expression.MatchCaseExpression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

import java.util.List;

@ToString(exclude = {"statements"})
@EqualsAndHashCode(exclude = "position")
@Value
public class MatchStatement implements Statement {
    String name;
    List<MatchCaseExpression> statements;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
