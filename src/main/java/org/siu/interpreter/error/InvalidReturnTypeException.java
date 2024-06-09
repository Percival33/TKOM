package org.siu.interpreter.error;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.siu.token.Position;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvalidReturnTypeException extends InterpreterException {
    String functionName;
    Position position;

    public InvalidReturnTypeException(String functionName, Position position) {
        super(position);
        this.functionName = functionName;
        this.position = position;
    }
}
