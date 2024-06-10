package org.siu.interpreter.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.siu.token.Position;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OperationNotSupported extends InterpreterException {
    private final String operationName;
    private final String type;

    public OperationNotSupported(String operationName, String type, Position position) {
        super(position);
        this.operationName = operationName;
        this.type = type;
    }
}
