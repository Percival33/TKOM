package org.siu.interpreter.state.value;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Value;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StringValue implements Value {
    String string;
    final TypeDeclaration type = new TypeDeclaration(ValueType.STRING);

    public StringValue(String string) {
        this.string = string;
    }

    @Override
    public StringValue copy() {
        return new StringValue(string);
    }
}
