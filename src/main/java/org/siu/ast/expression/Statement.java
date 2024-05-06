package org.siu.ast.expression;

import org.siu.ast.Node;
import org.siu.parser.Visitable;
import org.siu.token.Position;

public interface Statement extends Visitable, Node {
    Position getPosition();
}
