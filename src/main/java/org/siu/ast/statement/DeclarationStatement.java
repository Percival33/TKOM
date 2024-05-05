package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Argument;
import org.siu.ast.Node;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

import java.util.List;

@ToString(exclude = {"expression"})
@EqualsAndHashCode(exclude = "position")
@Value
public class DeclarationStatement implements Statement {
    Argument argument;
    Expression expression;
    Position position;
    public Iterable<Node> getExpression() { return List.of(expression); }

    @Override
    public void accept(Visitor visitor) {
         visitor.visit(this);
    }
}
