package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.Statement;
import org.siu.ast.expression.FunctionCallExpression;
import org.siu.ast.expression.IdentifierExpression;
import org.siu.ast.expression.StructMemberExpression;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.statement.AssignmentStatement;
import org.siu.ast.statement.StructMemberAssignmentStatement;
import org.siu.ast.type.IntegerExpression;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssignmentTest {
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

    private FunctionDefinitionStatement parseAndBuildFunction(String sourceCode) {
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();
        return program.getFunctionDefinitions().get("a");
    }

    private BlockStatement blockOf(Statement... statements) {
        return new BlockStatement(List.of(statements), position);
    }

    @Test
    void testAssignmentIntExpression() {
        FunctionDefinitionStatement function = parseAndBuildFunction("fn a() { x = 1; }");
        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(new AssignmentStatement("x", new IntegerExpression(1, position), position)),
                position
        );
        assertEquals(expected, function);
    }

    @Test
    void testAssignmentFnCallExpression() {
        FunctionDefinitionStatement function = parseAndBuildFunction("fn a() { x = foo(v); }");
        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(new AssignmentStatement("x", new FunctionCallExpression("foo", List.of(new IdentifierExpression("v", position)), position), position)), position);
        assertEquals(expected, function);
    }

    @Test
    void testAssignmentStructExpression() {
        FunctionDefinitionStatement function = parseAndBuildFunction("fn a() { p.x = foo(1); }");
        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(new StructMemberAssignmentStatement(new StructMemberExpression("p", "x", position), new FunctionCallExpression("foo", List.of(new IntegerExpression(1, position)), position), position)), position);
        assertEquals(expected, function);
    }
}
