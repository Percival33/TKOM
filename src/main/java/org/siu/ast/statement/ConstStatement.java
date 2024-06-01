package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Statement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@ToString(exclude = {"statement"})
@EqualsAndHashCode(exclude = "position")
@Value
public class ConstStatement implements Statement {
    String name;
    Statement statement;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
