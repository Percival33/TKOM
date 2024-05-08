package org.siu.ast.type;

import lombok.EqualsAndHashCode;
import lombok.Value;


@EqualsAndHashCode
@Value
public class TypeDeclaration {
    ValueType valueType;
    String customType;

    public TypeDeclaration(ValueType valueType) {
        this.valueType = valueType;
        customType = null;
    }

    public TypeDeclaration(ValueType valueType, String customType) {
        this.valueType = valueType;
        this.customType = customType;
        if (valueType != ValueType.CUSTOM && customType == null) throw new AssertionError();
    }
}
