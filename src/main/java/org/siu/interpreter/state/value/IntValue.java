package org.siu.interpreter.state.value;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Value;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntValue implements Value {
    @Setter
    int integer;
    private final TypeDeclaration type = new TypeDeclaration(ValueType.INT);

    public IntValue(int integer) {
        this.integer = integer;
    }
}
