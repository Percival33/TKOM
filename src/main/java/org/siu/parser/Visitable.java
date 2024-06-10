package org.siu.parser;

import org.siu.interpreter.Visitor;

public interface Visitable {
    void accept(Visitor visitor);
}
