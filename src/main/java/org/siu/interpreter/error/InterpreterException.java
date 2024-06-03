package org.siu.interpreter.error;


import lombok.*;
import org.siu.token.Position;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class InterpreterException extends RuntimeException {
    private Position position;

    @Override
    public String getMessage() {
        return this + " - " + getClass().getName();
    }
}
