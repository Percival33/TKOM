package org.siu.ast;

import org.siu.interpreter.Visitor;
import org.siu.token.Position;

public interface Node {
    Position getPosition();
    void accept(Visitor visitor);
}
