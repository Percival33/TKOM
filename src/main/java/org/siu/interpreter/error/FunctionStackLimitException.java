package org.siu.interpreter.error;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.siu.ast.expression.FunctionCallExpression;
import org.siu.token.Position;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FunctionStackLimitException extends InterpreterException {
    private String identifier;

    public FunctionStackLimitException(String identifier, Position position) {
        super(position);
        this.identifier = identifier;
    }
}
