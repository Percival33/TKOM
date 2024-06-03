package org.siu.interpreter.state;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.siu.token.Position;

import java.util.*;

@RequiredArgsConstructor
public class Context {
    @Getter
    private final String functionName;
    @Getter
    private final Position position;
    private final Deque<Scope> scopes = new ArrayDeque<>(List.of(new Scope()));

    public void incrementScope() {
        scopes.add(new Scope());
    }

    public void decrementScope() {
        scopes.removeLast();
    }

    public Optional<Variable> findVariable(String identifier) {
        for (Iterator<Scope> it = scopes.descendingIterator(); it.hasNext(); ) {
            var scope = it.next();
            var variable = scope.findVariable(identifier);
            if (variable.isPresent()) {
                return variable;
            }
        }
        return Optional.empty();
    }

    public void addVariable(Variable variable) {
        var scope = scopes.getLast();
        scope.addVariable(variable);
    }

    public boolean updateVariable(String identifier, Value value) {
        for(Iterator<Scope> it = scopes.descendingIterator(); it.hasNext(); ) {
            var scope = it.next();
            if (scope.updateVariable(identifier, value)) {
                return true;
            }
        }
        return false;
    }
}
