package org.siu.interpreter.state.value;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Value;

import java.io.Serial;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloatValue implements Value {
    @Setter
    float floatVal;
    private final TypeDeclaration type = new TypeDeclaration(ValueType.FLOAT);

    public FloatValue(float floatVal) {
        this.floatVal = floatVal;
    }

    @Override
    public FloatValue copy() {
        return new FloatValue(floatVal);
    }
}
