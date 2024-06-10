package org.siu.interpreter.error;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.siu.token.Position;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidTypeAssignmentException extends InterpreterException {
    String typeName = "";
    public InvalidTypeAssignmentException(Position position) {
        super(position);
    }

    public InvalidTypeAssignmentException(String typeName) {
        super();
        this.typeName = typeName;
    }
}
