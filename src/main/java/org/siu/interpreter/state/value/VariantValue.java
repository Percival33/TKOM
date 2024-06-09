package org.siu.interpreter.state.value;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.siu.ast.Parameter;
import org.siu.ast.type.TypeDeclaration;
import org.siu.interpreter.error.NotExistingVariantTypeException;
import org.siu.interpreter.error.TypesDoNotMatchException;
import org.siu.interpreter.state.Value;

import java.util.HashMap;
import java.util.Map;

@lombok.Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantValue implements Value {
    TypeDeclaration type;
    @Getter
    Map<String, Parameter> variantMembers;
    @Setter
    Value value;
    @Getter
    String currentField;

    public VariantValue(TypeDeclaration typeDeclaration, Map<String, Parameter> copiedVariantMembers, String field, Value value) {
        this.type = typeDeclaration;
        this.variantMembers = new HashMap<>(copiedVariantMembers);
        this.currentField = field;
        this.value = value;

        if (!variantMembers.containsKey(field)) {
            throw new NotExistingVariantTypeException(type.getCustomType(), field);
        }
    }

    @Override
    public Value get() {
        if (value == null) {
            throw new RuntimeException("Variant value is not initialized");
        }

        return value;
    }

    @Override
    public VariantValue copy() {
        Map<String, Parameter> copiedVariantMembers = new HashMap<>();
        for (Map.Entry<String, Parameter> entry : variantMembers.entrySet()) {
            copiedVariantMembers.put(entry.getKey(), new Parameter(entry.getValue().getType(), entry.getValue().getName()));
        }

        return new VariantValue(
                new TypeDeclaration(type.getValueType(), type.getCustomType()),
                copiedVariantMembers,
                currentField,
                value == null ? null : value.copy()
        );
    }
    @Override
    public boolean isVariant() {
        return true;
    }
}
