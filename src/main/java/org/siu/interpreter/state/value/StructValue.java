package org.siu.interpreter.state.value;

import lombok.Getter;
import org.siu.ast.type.TypeDeclaration;
import org.siu.interpreter.error.NotExistingStructMemberException;
import org.siu.interpreter.error.StructMemberNotInitializedException;
import org.siu.interpreter.error.TypesDoNotMatchException;
import org.siu.interpreter.state.Value;

import java.util.Map;
import java.util.stream.Collectors;

@lombok.Value
public class StructValue implements Value {
    TypeDeclaration type;
    @Getter
    Map<String, Value> structMembers;

    @Override
    public Value get(String key) {
        if (!structMembers.containsKey(key)) {
            throw new NotExistingStructMemberException(type.getCustomType(), key);
        }

        var value = structMembers.get(key);

        if (value == null) {
            throw new StructMemberNotInitializedException(type.getCustomType(), key);
        }

        return value;
    }

    @Override
    public void put(String key, Value value) {
        var previous = structMembers.put(key, value);
        if (previous == null) {
            throw new StructMemberNotInitializedException(type.getCustomType(), key);
        }

        if (!previous.getType().equals(value.getType())) {
            throw new TypesDoNotMatchException(value.getType(), previous.getType());
        }

        structMembers.put(key, value);
    }

    @Override
    public StructValue copy() {
        var newStructMembers = this.structMembers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().copy()
                ));
        return new StructValue(new TypeDeclaration(type.getValueType(), type.getCustomType()), newStructMembers);
    }

    @Override
    public boolean isStruct() {
        return true;
    }
}
