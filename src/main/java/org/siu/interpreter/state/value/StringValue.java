package org.siu.interpreter.state.value;

import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Value;

@lombok.Value
public class StringValue implements Value {
    String value;
    TypeDeclaration type = new TypeDeclaration(ValueType.STRING);
}
