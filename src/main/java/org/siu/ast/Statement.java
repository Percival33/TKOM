package org.siu.ast;

import org.siu.parser.Visitable;
import org.siu.token.Position;

public interface Statement extends Visitable, Node {
    Position getPosition();
    default String getName() {
        throw new RuntimeException("getName - not implemented for" + this.getClass().getName());
    }
}
