package org.siu.ast;

import lombok.ToString;
import lombok.Value;
import org.siu.ast.function.FunctionDefinition;
import org.siu.ast.statement.DeclarationStatement;

import java.util.Map;
@ToString(exclude = {"functionDefinitions", "declarations"})
@Value
public class Program {
    // TODO: change name of variable
    private final Map<String, FunctionDefinition> functionDefinitions;
    private final Map<String, DeclarationStatement> delarations;

}
