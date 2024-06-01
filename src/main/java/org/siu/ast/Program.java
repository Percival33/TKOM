package org.siu.ast;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.function.FunctionDefinition;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.Map;

@ToString(exclude = {"functionDefinitions", "declarations"})
@Value
@Getter
public class Program implements Node {
    Map<String, FunctionDefinition> functionDefinitions;
    Map<String, Statement> declarations;

    @Override
    public Position getPosition() {
        return new Position(1, 1);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
