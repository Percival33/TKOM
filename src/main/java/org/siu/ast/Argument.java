package org.siu.ast;

import lombok.Value;
import org.siu.ast.type.TypeDeclaration;

@Value
public class Argument {
    TypeDeclaration type;
    String name;
}
