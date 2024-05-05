package org.siu.ast.statement;

import org.siu.parser.Visitor;
import org.siu.token.Position;

public class IfStatement implements Statement{
    @Override
    public void accept(Visitor visitor) {

    }

    @Override
    public Position getPosition() {
        return null;
    }
}
