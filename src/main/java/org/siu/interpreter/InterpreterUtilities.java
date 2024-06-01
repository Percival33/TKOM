package org.siu.interpreter;

import org.siu.ast.BlockStatement;
import org.siu.ast.function.FunctionDefinitionStatement;

import java.util.HashMap;

public class InterpreterUtilities {
    //    public static final Position DEFAULT_POSITION ;
    public static final HashMap<String, FunctionDefinitionStatement> BUILTIN_FUNCTIONS = null;
//    public static final HashMap<String, FunctionDefinitionStatement> BUILTIN_FUNCTIONS = Map.of(
//            "print", new FunctionDefinitionStatement(
//                    "print",
//                    List.of(new Argument(PRINT_ARGUMENT, new TypeDeclaration(ValueType.STRING))),
//                    null,
//                    new BlockStatement(List.of(new PrintFunction()), DEFAULT_POSITION), DEFAULT_POSITION)
//            );
}
