package org.siu.interpreter.state;

import org.siu.ast.type.TypeDeclaration;
import org.siu.interpreter.error.UnexpectedTypeException;

import java.util.Optional;

public interface Value {
    TypeDeclaration getType();

    default int getInteger() {
        throw new UnexpectedTypeException();
    }

    default boolean isBool() {
        throw new UnexpectedTypeException();
    }

    default float getFloat() {
        throw new UnexpectedTypeException();
    }

    default String getString() {
        throw new UnexpectedTypeException();
    }

    default Optional<Value> getStructElement(String identifier) {
        // TODO: validate idea
        throw new UnexpectedTypeException();
    }

    default Optional<Value> getVariant(String identifier) {
        // TODO: validate idea
        throw new UnexpectedTypeException();
    }
}
