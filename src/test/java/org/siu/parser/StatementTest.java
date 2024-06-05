package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Parameter;
import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.Statement;
import org.siu.ast.expression.*;
import org.siu.ast.statement.MatchStatement;
import org.siu.ast.expression.relation.LessExpression;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.statement.*;
import org.siu.ast.type.BooleanExpression;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatementTest {
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
    void testIfStatement() {
        String sourceCode = "fn a() { if (true) { return 1; }  }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        Statement oneReturn = new ReturnStatement(new IntegerExpression(1, position), position);

        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position)),
                List.of(new BlockStatement(List.of(oneReturn), position)),
                Optional.empty(),
                position
        );
        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                new BlockStatement(List.of(ifStatement), position),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testIfStatementWithElse() {
        String sourceCode = "fn a() { if (true) { return 1; } else { return 2; } }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);

        Statement oneReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        BlockStatement elseBlock = new BlockStatement(List.of(new ReturnStatement(new IntegerExpression(2, position), position)), position);

        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position)),
                List.of(new BlockStatement(List.of(oneReturn), position)),
                Optional.of(elseBlock),
                position
        );

        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                new BlockStatement(List.of(ifStatement), position),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testIfStatementWithElif() {
        String sourceCode = "fn a() { if (true) { return 1; } elif(1 < 2) { return 2; } }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);

        Statement trueReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        Statement twoReturn = new ReturnStatement(new IntegerExpression(2, position), position);

        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position), new LessExpression(new IntegerExpression(1, position), new IntegerExpression(2, position), position)),
                List.of(new BlockStatement(List.of(trueReturn), position), new BlockStatement(List.of(twoReturn), position)),
                Optional.empty(),
                position
        );

        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                new BlockStatement(List.of(ifStatement), position),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testIfStatementWithElifAndElse() {
        String sourceCode = "fn a() { if (true) { return 1; } elif(1 < 2) { return 2; } else { return 3; } }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);

        Statement trueReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        Statement twoReturn = new ReturnStatement(new IntegerExpression(2, position), position);
        Statement threeReturn = new ReturnStatement(new IntegerExpression(3, position), position);
        BlockStatement elseBlock = blockOf(threeReturn);

        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position), new LessExpression(new IntegerExpression(1, position), new IntegerExpression(2, position), position)),
                List.of(blockOf(trueReturn), blockOf(twoReturn)),
                Optional.of(elseBlock),
                position
        );

        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(ifStatement),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testWhileStatement() {
        String sourceCode = "fn a() { while (true) { return 1; } }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);

        Statement whileStatement = new WhileStatement(
                new BooleanExpression(true, position),
                new BlockStatement(
                        List.of(new ReturnStatement(
                                new IntegerExpression(1, position),
                                position
                        )),
                        position
                ),
                position
        );

        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(whileStatement),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testAssignmentStatement() {
        String sourceCode = "fn a() { bool b = 0; abc = 1; }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);

        Statement declaratioinStatement = new DeclarationStatement(
                new Parameter(new TypeDeclaration(ValueType.BOOL), "b"),
                new IntegerExpression(0, position),
                position
        );
        Statement assignmentStatement = new AssignmentStatement(
                "abc",
                new IntegerExpression(1, position),
                position
        );
        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(declaratioinStatement, assignmentStatement),
                position
        );
        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testCopyValueStatement() {
        String sourceCode = "fn a() { f(@abc); }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);

        Expression argument = new CopiedValueExpression(
                new IdentifierExpression("abc", position),
                position
        );
        Statement functionCall = new FunctionCallExpression("f", List.of(argument), position);

        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(functionCall),
                position
        );
        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testVariantDeclaration() {
        String sourceCode = "fn a() { Var v = Var::row(3); }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);

        Statement declaratioinStatement = new DeclarationStatement(
                new Parameter(new TypeDeclaration(ValueType.CUSTOM, "Var"), "v"),
                new VariantExpression("row", new IntegerExpression(3, position), position),
                position
        );

        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(declaratioinStatement),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testMatchStatement() {
        String sourceCode = """
                fn a(Var v): Var {
                    match(v) {
                        Var::row(x) { x }
                        Var::col(y) { y }
                    }
                    return value;
                }
                """;
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);

        MatchCaseExpression rowCase = new MatchCaseExpression("Var", "row", "x", new IdentifierExpression("x", position), position);
        MatchCaseExpression colCase = new MatchCaseExpression("Var", "col", "y", new IdentifierExpression("y", position), position);
        Statement matchStatement = new MatchStatement("v", List.of(rowCase, colCase), position);
        Statement returnStatement = new ReturnStatement(new IdentifierExpression("value", position), position);

        FunctionDefinitionStatement expectedFunction = new FunctionDefinitionStatement(
                "a",
                List.of(new Parameter(new TypeDeclaration(ValueType.CUSTOM, "Var"), "v")),
                Optional.of(new TypeDeclaration(ValueType.CUSTOM, "Var")),
                blockOf(matchStatement, returnStatement),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }
}
