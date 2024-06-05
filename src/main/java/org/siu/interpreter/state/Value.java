package org.siu.interpreter.state;

import org.siu.ast.type.TypeDeclaration;
import org.siu.interpreter.error.UnexpectedTypeException;

public interface Value {
    TypeDeclaration getType();

    default int getInteger() {
        throw new UnexpectedTypeException();
    }

    default boolean isBool() {
        throw new UnexpectedTypeException();
    }

    default float getFloatVal() {
        throw new UnexpectedTypeException();
    }

    default String getString() {
        throw new UnexpectedTypeException();
    }
}
