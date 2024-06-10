package org.siu.interpreter.builtin;

import lombok.Value;
import org.siu.ast.Statement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@Value
public class PrintFunction implements Statement {

    @Override
    public Position getPosition() {
        return new Position(1, 1);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}