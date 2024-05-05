package org.siu.ast;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.function.FunctionDefinition;
import org.siu.ast.statement.DeclarationStatement;

import java.util.Map;
@ToString(exclude = {"functionDefinitions", "declarations"})
@Value
@Getter
public class Program {
    Map<String, FunctionDefinition> functionDefinitions;
    Map<String, DeclarationStatement> declarations;
}
