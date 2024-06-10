package org.siu.interpreter.error;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.siu.token.Position;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnexpectedTypeException extends InterpreterException {
    String typeName = "";
    public UnexpectedTypeException(Position position) {
        super(position);
    }

    public UnexpectedTypeException(String typeName, Position position) {
        super(position);
        this.typeName = typeName;
    }
}
