package org.siu.interpreter;

import lombok.experimental.UtilityClass;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.interpreter.state.Context;
import org.siu.token.Position;

import java.util.HashMap;

@UtilityClass
public class InterpreterUtilities {
    public static final Position DEFAULT_POSITION = new Position(1, 1);
    public static final Context GLOBAL_CONTEXT = new Context("main", DEFAULT_POSITION);
    public static final HashMap<String, FunctionDefinitionStatement> BUILTIN_FUNCTIONS = null;
//    public static final HashMap<String, FunctionDefinitionStatement> BUILTIN_FUNCTIONS = Map.of(
//            "print", new FunctionDefinitionStatement(
//                    "print",
//                    List.of(new Argument(PRINT_ARGUMENT, new TypeDeclaration(ValueType.STRING))),
//                    null,
//                    new BlockStatement(List.of(new PrintFunction()), DEFAULT_POSITION), DEFAULT_POSITION)
//            );

    public static final TypeDeclaration INT_TYPE = new TypeDeclaration(ValueType.INT);
    public static final TypeDeclaration FLOAT_TYPE = new TypeDeclaration(ValueType.FLOAT);
    public static final TypeDeclaration BOOL_TYPE = new TypeDeclaration(ValueType.BOOL);
    public static final TypeDeclaration STRING_TYPE = new TypeDeclaration(ValueType.STRING);
    public static final TypeDeclaration VARIANT_TYPE = new TypeDeclaration(ValueType.VARIANT);
    public static final TypeDeclaration STRUCT_TYPE = new TypeDeclaration(ValueType.STRUCT);
}
