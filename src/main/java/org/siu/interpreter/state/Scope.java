package org.siu.interpreter.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.siu.interpreter.error.DuplicatedVariableException;
import org.siu.interpreter.error.TypesDoNotMatchException;

@lombok.Value
public class Scope {
    Map<String, Variable> variables = new HashMap<>();

    public void addVariable(Variable variable) {
        if (variables.containsKey(variable.getIdentifier())) {
            throw new DuplicatedVariableException(variable.getIdentifier());
        }
        variables.put(variable.getIdentifier(), variable);
    }

    public boolean updateVariable(String identifier, Value value) {
        if (!variables.containsKey(identifier)) {
            return false;
        }
        var previousValue = variables.get(identifier);
        if (!Objects.equals(previousValue.getType(), value.getType())) {
            throw new TypesDoNotMatchException(value.getType(), previousValue.getType());
        }
        variables.put(identifier, new Variable(previousValue.getType(), identifier, value));
        return true;
    }

    public Optional<Variable> findVariable(String identifier) {
        return Optional.ofNullable(variables.get(identifier));
    }
}
