package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Argument;
import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.Statement;
import org.siu.ast.expression.Expression;
import org.siu.ast.expression.FunctionCallExpression;
import org.siu.ast.expression.IdentifierExpression;
import org.siu.ast.expression.arithmetic.AddArithmeticExpression;
import org.siu.ast.expression.arithmetic.MultiplyArithmeticExpression;
import org.siu.ast.function.FunctionDefinition;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.statement.ReturnStatement;
import org.siu.ast.type.IntegerExpression;
import org.siu.ast.type.TypeDeclaration;
import org.siu.ast.type.ValueType;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FunctionDefinitionTest {
    private ErrorHandler errorHandler;
    private Position position;

    @BeforeEach
    void setUp() {
        errorHandler = Mockito.mock(ErrorHandler.class);
        position = Mockito.mock(Position.class);
    }

    Parser toParser(String text) {
        Lexer lexer = new LexerImpl(text, errorHandler);
        return new Parser(lexer, errorHandler);
    }

    @Test
    void fnDefinitionTest() {
        String s = "fn add(int a, int b): int { return a + b; }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        var fn = program.getFunctionDefinitions().get("add");
        assertEquals(new FunctionDefinition(
                        "add",
                        List.of(new Argument(new TypeDeclaration(ValueType.INT), "a"), new Argument(new TypeDeclaration(ValueType.INT), "b")),
                        Optional.of(new TypeDeclaration(ValueType.INT)),
                        new BlockStatement(
                                List.of(new ReturnStatement(
                                        new AddArithmeticExpression(
                                                new IdentifierExpression("a", position),
                                                new IdentifierExpression("b", position),
                                                position
                                        ),
                                        position)),
                                position
                        ),
                        position),
                fn);
    }

    @Test
    void fnDefinitionWithNoReturnValue() {
        String s = "fn add(int a) {}";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        var fn = program.getFunctionDefinitions().get("add");
        assertEquals(new FunctionDefinition(
                        "add",
                        List.of(new Argument(new TypeDeclaration(ValueType.INT), "a")),
                        Optional.empty(),
                        new BlockStatement(List.of(), position),
                        position),
                fn);
    }

    @Test
    void fnDefinitionWithNoArguments() {
        String s = "fn add() { f(1); }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        var fn = program.getFunctionDefinitions().get("add");
        assertEquals(new FunctionDefinition(
                        "add",
                        List.of(),
                        Optional.empty(),
                        new BlockStatement(List.of(new FunctionCallExpression("f", List.of(new IntegerExpression(1, position)), position)), position),
                        position),
                fn);
    }

    @Test
    void fnDefinitionTest2() {
        String s = "fn fun(int a, int b): float { int c = 5; return a + b * c; }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        var fn = program.getFunctionDefinitions().get("fun");
        BlockStatement block = getBlockStatement();
        FunctionDefinition expected = new FunctionDefinition(
                "fun",
                List.of(new Argument(new TypeDeclaration(ValueType.INT), "a"), new Argument(new TypeDeclaration(ValueType.INT), "b")),
                Optional.of(new TypeDeclaration(ValueType.FLOAT)),
                block,
                position);

        assertEquals(expected.getName(), fn.getName());
        assertEquals(expected.getParameters(), fn.getParameters());
        assertEquals(expected.getReturnType(), fn.getReturnType());
        assertEquals(expected.getBlock(), fn.getBlock());
    }

    private BlockStatement getBlockStatement() {
        Statement declarationStatement = new DeclarationStatement(new Argument(new TypeDeclaration(ValueType.INT), "c"), new IntegerExpression(5, position), position);
        Statement returnStatement = new ReturnStatement(new AddArithmeticExpression(new IdentifierExpression("a", position), new MultiplyArithmeticExpression(new IdentifierExpression("b", position), new IdentifierExpression("c", position), position), position), position);
        return new BlockStatement(List.of(declarationStatement, returnStatement), position);
    }
}
