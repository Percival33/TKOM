package org.siu.interpreter.state;

import lombok.Getter;
import org.siu.ast.type.TypeDeclaration;
import org.siu.interpreter.error.UnexpectedTypeException;

public interface Value {
    TypeDeclaration getType();

    default int getInteger() {
        throw new UnexpectedTypeException();
    }

    default void setInteger(int value) {
        throw new UnexpectedTypeException();
    }

    default boolean isBool() {
        throw new UnexpectedTypeException();
    }

    default void setBool(boolean value) {
        throw new UnexpectedTypeException();
    }

    default float getFloatVal() {
        throw new UnexpectedTypeException();
    }

    default void setFloatVal(float floatVal) {
        throw new UnexpectedTypeException();
    }

    default String getString() {
        throw new UnexpectedTypeException();
    }

    default void setString(String string) {
        throw new UnexpectedTypeException();
    }

    // struct
    default Value get(String key) {
        throw new UnexpectedTypeException();
    }

    default void put(String key, Value value) {
        throw new UnexpectedTypeException();
    }

    // variant
    default Value get() {
        throw new UnexpectedTypeException();
    }

    default void put(Value value) {
        throw new UnexpectedTypeException();
    }

    default Value copy() {
        throw new UnexpectedTypeException();
    }

    default boolean isStruct() { throw new UnexpectedTypeException(); }
}
