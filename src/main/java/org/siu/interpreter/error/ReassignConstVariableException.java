package org.siu.interpreter.error;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.siu.token.Position;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReassignConstVariableException extends InterpreterException {
    String variableName;
    Position position;

    public ReassignConstVariableException(String variableName, Position position) {
        super(position);
        this.variableName = variableName;
        this.position = position;
    }
}
