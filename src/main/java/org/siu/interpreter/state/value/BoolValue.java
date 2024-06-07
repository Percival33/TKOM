package org.siu.interpreter.state.value;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Value;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoolValue implements Value {
    boolean bool;
    private final TypeDeclaration type = new TypeDeclaration(ValueType.BOOL);

    public BoolValue(boolean bool) {
        this.bool = bool;
    }

    @Override
    public BoolValue copy() {
        return new BoolValue(bool);
    }
}
