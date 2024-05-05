package org.siu.ast;

import lombok.Value;
import org.siu.ast.type.ValueType;

@Value
public class Argument {
    ValueType type;
    String name;
}
