package org.siu.ast.expression;

import org.siu.parser.Visitor;
import org.siu.token.Position;

public class LogicFactor implements Expression{
    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public void accept(Visitor visitor) {

    }
}
