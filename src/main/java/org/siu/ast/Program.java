package org.siu.ast;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.Map;

@ToString(exclude = {"functionDefinitions", "declarations"})
@Value
@Getter
public class Program implements Node {
    Map<String, FunctionDefinitionStatement> functionDefinitions;
    Map<String, Statement> declarations;
    Map<String, Statement> typeDefinitions;

    @Override
    public Position getPosition() {
        return new Position(1, 1);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
