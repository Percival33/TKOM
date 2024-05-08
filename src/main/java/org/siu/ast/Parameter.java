package org.siu.ast;

import lombok.Value;
import org.siu.ast.type.TypeDeclaration;

@Value
public class Parameter {
    TypeDeclaration type;
    String name;
}
