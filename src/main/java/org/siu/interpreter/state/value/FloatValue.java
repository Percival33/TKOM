package org.siu.interpreter.state.value;

import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Value;

@lombok.Value
public class FloatValue implements Value {
    float floatVal;
    TypeDeclaration type = new TypeDeclaration(ValueType.FLOAT);
}
