package org.siu.ast.function;

import lombok.Getter;
import org.siu.ast.Block;
import org.siu.token.Position;

import java.util.List;
import java.util.Optional;

@Getter
public class FunctionDefinition {
    List<FunctionParameter> parameters;
    Optional<FunctionParameter> returnType;
    String name;
    Position position;

    public FunctionDefinition(String name, List<FunctionParameter> parameters, Optional<FunctionParameter> returnType, Block block, Position position) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.position = position;
    }
}
