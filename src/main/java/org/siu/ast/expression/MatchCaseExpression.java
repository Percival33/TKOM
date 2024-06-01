package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Statement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;


@ToString(exclude = {"expression"})
@EqualsAndHashCode(exclude = "position")
@Value
public class MatchCaseExpression implements Statement {
    String variantType;
    String member;
    String variable;
    Expression expression;

    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
