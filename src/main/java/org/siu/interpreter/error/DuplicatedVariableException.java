package org.siu.interpreter.error;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DuplicatedVariableException extends InterpreterException {
    String identifier;
}
