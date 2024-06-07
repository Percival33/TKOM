package org.siu.interpreter.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.vavr.Function2;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.error.DuplicatedVariableException;
import org.siu.interpreter.error.TypesDoNotMatchException;
import org.siu.interpreter.state.value.IntValue;

@lombok.Value
public class Scope {
    Map<String, Variable> variables = new HashMap<>();

    public void addVariable(Variable variable) {
        if (variables.containsKey(variable.getIdentifier())) {
            throw new DuplicatedVariableException(variable.getIdentifier());
        }
        variables.put(variable.getIdentifier(), variable);
    }

    // Create a private static final map with mappers for each Value type that will update the value of the variable
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
            throw new RuntimeException("Unsupported value type: " + previousValue.getType().getCustomType());
        }
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
