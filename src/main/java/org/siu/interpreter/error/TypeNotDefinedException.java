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
public class TypeNotDefinedException extends InterpreterException{
    String typeName;
    public TypeNotDefinedException(String typeName, Position position) {
        super(position);
        this.typeName = typeName;
    }
}
