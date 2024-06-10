package org.siu.interpreter.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.siu.token.Position;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArithmeticOperationNotSupportedForNonNumericTypes extends InterpreterException {
    public ArithmeticOperationNotSupportedForNonNumericTypes(Position position) {
        super(position);
        this.position = position;
    }
}
