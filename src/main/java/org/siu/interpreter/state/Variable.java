package org.siu.interpreter.state;

import org.siu.ast.type.TypeDeclaration;

import java.util.Objects;

import org.siu.interpreter.error.TypesDoNotMatchException;

@lombok.Value
public class Variable {
    TypeDeclaration type;
    String identifier;
    Value value;

    public Variable(TypeDeclaration type, String identifier, Value value) {
        this.type = type;
        this.identifier = identifier;
        this.value = value;

        if (!Objects.equals(type, value.getType())) {
            throw new TypesDoNotMatchException(value.getType(), type);
        }
    }
}
