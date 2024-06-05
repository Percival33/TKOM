package org.siu.interpreter.state.value;

import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Value;

import java.util.Map;

@lombok.Value
public class StructValue implements Value {
    String string;
    TypeDeclaration type;

    Map<String, Value> structMembers;
}
