package org.siu.ast;

import org.siu.parser.Visitable;
import org.siu.token.Position;

public interface Statement extends Visitable, Node {
    Position getPosition();
}
