package org.siu.interpreter.error;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.siu.token.Position;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DuplicatedVariableException extends InterpreterException {
    String identifier;

    public DuplicatedVariableException(String Identifier, Position position) {
        super();
        this.identifier = Identifier;
        this.position = position;
    }
}
