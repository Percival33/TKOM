package org.siu.ast;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.List;

@ToString(exclude = {"statements"})
@EqualsAndHashCode(exclude = "position")
@Value
public class BlockStatement implements Statement {
    List<Statement> statements;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
