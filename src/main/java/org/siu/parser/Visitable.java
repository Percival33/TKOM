package org.siu.parser;

public interface Visitable {
    public accept(Visitor visitor);
}
