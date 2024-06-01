package org.siu.parser;

public interface Visitable {
    void accept(Visitor visitor);
}
