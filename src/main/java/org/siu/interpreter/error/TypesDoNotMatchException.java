package org.siu.interpreter.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.siu.ast.type.TypeDeclaration;
import org.siu.token.Position;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TypesDoNotMatchException extends InterpreterException {
    TypeDeclaration provided;
    TypeDeclaration expected;

    public TypesDoNotMatchException(TypeDeclaration provided, TypeDeclaration expected, Position position) {
        super(position);
        this.provided = provided;
        this.expected = expected;
    }
}
