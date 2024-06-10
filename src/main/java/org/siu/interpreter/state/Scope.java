package org.siu.interpreter.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.vavr.Function2;
import org.apache.commons.lang3.StringUtils;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.error.DuplicatedVariableException;
import org.siu.interpreter.error.InvalidTypeAssignmentException;
import org.siu.interpreter.error.TypesDoNotMatchException;
import org.siu.interpreter.state.value.IntValue;
import org.siu.interpreter.state.value.StructValue;
import org.siu.interpreter.state.value.VariantValue;

@lombok.Value
public class Scope {
    Map<String, Variable> variables = new HashMap<>();

    public void addVariable(Variable variable) {
        if (variables.containsKey(variable.getIdentifier())) {
            throw new DuplicatedVariableException(variable.getIdentifier());
        }
        variables.put(variable.getIdentifier(), variable);
    }

    private final static Map<ValueType, BiConsumer<Value, Value>> MAPPERS = Map.of(
            ValueType.INT, (previousValue, newValue) -> previousValue.setInteger(newValue.getInteger()),
            ValueType.FLOAT, (previousValue, newValue) -> previousValue.setFloatVal(newValue.getFloatVal()),
            ValueType.STRING, (previousValue, newValue) -> previousValue.setString(newValue.getString()),
            ValueType.BOOL, (previousValue, newValue) -> previousValue.setBool(newValue.isBool())
    );

    private void update(Value previousValue, Value newValue) {
        var updateFunction = MAPPERS.get(previousValue.getType().getValueType());
        if (updateFunction != null) {
            updateFunction.accept(previousValue, newValue);
        } else {
            if (!newValue.isStruct() && !previousValue.isVariant()) {
                throw new InvalidTypeAssignmentException(newValue.getType().toString());
            }

            if(!StringUtils.equals(previousValue.getType().getCustomType(), newValue.getType().getCustomType())) {
                throw new TypesDoNotMatchException(newValue.getType(), previousValue.getType());
            }

            updateStruct(previousValue, newValue);
            updateVariant(previousValue, newValue);
        }
    }

    private void updateVariant(Value previousValue, Value newValue) {
        if(!newValue.isVariant()) return;

        var previousVariantValue = (VariantValue) previousValue;
        var newVariantValue = (VariantValue) newValue;

        if(previousVariantValue.getVariantMembers().containsKey(newVariantValue.getCurrentField())) {
            previousVariantValue.setCurrentField(newVariantValue.getCurrentField());
            previousVariantValue.setValue(newVariantValue.getValue());
        } else {
            throw new TypesDoNotMatchException(previousValue.getType(), newValue.getType());
        }
    }

    private void updateStruct(Value previousValue, Value newValue) {
        if(!newValue.isStruct()) return;

        var previousStructValue = (StructValue) previousValue;
        var newStructValue = (StructValue) newValue;

        previousStructValue.getStructMembers().forEach((key, value) -> {
            var newValueForKey = newStructValue.getStructMembers().get(key);
            if (newValueForKey == null) {
                throw new TypesDoNotMatchException(previousValue.getType(), newValue.getType());
            }
            previousStructValue.put(key, newValueForKey);
        });
    }

    public boolean updateVariable(String identifier, Value value) {
        if (!variables.containsKey(identifier)) {
            return false;
        }
        var previousValue = variables.get(identifier);
        if (!Objects.equals(previousValue.getType(), value.getType())) {
            throw new TypesDoNotMatchException(value.getType(), previousValue.getType());
        }

        update(previousValue.getValue(), value);
        return true;
    }

    public Optional<Variable> findVariable(String identifier) {
        return Optional.ofNullable(variables.get(identifier));
    }
}
