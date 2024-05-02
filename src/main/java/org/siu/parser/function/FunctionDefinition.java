package org.siu.parser.function;

import lombok.Getter;
import org.siu.parser.Block;
import org.siu.parser.ProgramElement;
import org.siu.token.Position;

import java.util.List;
import java.util.Optional;

@Getter
public class FunctionDefinition extends ProgramElement {
    List<FunctionParameter> parameters;
    Optional<FunctionParameter> returnType;

    public FunctionDefinition(String name, List<FunctionParameter> parameters, Optional<FunctionParameter> returnType, Block block, Position position) {
        super(name, position);
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.position = position;
    }
}
