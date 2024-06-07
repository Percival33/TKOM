package org.siu.ast.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.BlockStatement;
import org.siu.ast.Statement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;


@ToString(exclude = {"block"})
@EqualsAndHashCode(exclude = "position")
@Value
public class MatchCaseStatement implements Statement {
    String variantType;
    String member;
    String variable;
    BlockStatement block;

    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
