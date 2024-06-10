package org.siu.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.siu.ast.type.TypeDeclaration;

@EqualsAndHashCode
@Value
public class Parameter {
    TypeDeclaration type;
    String name;
}
