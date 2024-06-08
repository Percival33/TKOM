package org.siu.interpreter.error;


import lombok.*;
import org.siu.token.Position;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class InterpreterException extends RuntimeException {
    protected Position position;

    public InterpreterException(Position position) {
        super();
        this.position = position;
    }

    @Override
    public String getMessage() {
        return this + " - " + getClass().getName();
    }
}
