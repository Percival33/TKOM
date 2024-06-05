package org.siu.interpreter.error;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.siu.token.Position;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReassignConstVariableException extends InterpreterException {
    String variableName;
    Position position;
}
