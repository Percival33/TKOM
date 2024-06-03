package org.siu.interpreter.state.value;

import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Value;

@lombok.Value
public class IntValue implements Value {
    Integer integer;
    TypeDeclaration type = new TypeDeclaration(ValueType.INT);
}
