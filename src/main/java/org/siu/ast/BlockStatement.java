package org.siu.ast;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.List;

@ToString(exclude = {"statementList"})
@EqualsAndHashCode(exclude = "position")
@Value
public class BlockStatement implements Statement {
    List<Statement> statementList;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
