package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Argument;
import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.Statement;
import org.siu.ast.expression.VariantExpression;
import org.siu.ast.expression.relation.LessExpression;
import org.siu.ast.function.FunctionDefinition;
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

    private FunctionDefinition parseAndBuildFunction(String sourceCode) {
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();
        return program.getFunctionDefinitions().get("a");
    }

    private BlockStatement blockOf(Statement... statements) {
        return new BlockStatement(List.of(statements), position);
    }

    @Test
    void testIfStatement() {
        String s = "fn a() { if (true) { return 1; }  }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Statement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position)),
                List.of(new BlockStatement(
                        List.of(new ReturnStatement(
                                new IntegerExpression(1, position),
                                position
                        )),
                        position
                )),
                Optional.empty(),
                position
        );
        var fn = program.getFunctionDefinitions().get("a");
        assertEquals(new FunctionDefinition(
                        "a",
                        List.of(),
                        Optional.empty(),
                        new BlockStatement(
                                List.of(ifStatement),
                                position),
                        position),
                fn);
    }

    @Test
    void testIfStatementWithElse() {
        String s = "fn a() { if (true) { return 1; } else { return 2; } }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Statement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position)),
                List.of(new BlockStatement(
                        List.of(new ReturnStatement(
                                new IntegerExpression(1, position),
                                position
                        )),
                        position
                )),
                Optional.of(new BlockStatement(
                        List.of(new ReturnStatement(
                                new IntegerExpression(2, position),
                                position
                        )),
                        position
                )),
                position
        );
        var fn = program.getFunctionDefinitions().get("a");
        assertEquals(new FunctionDefinition(
                        "a",
                        List.of(),
                        Optional.empty(),
                        new BlockStatement(
                                List.of(ifStatement),
                                position),
                        position),
                fn);
    }

    @Test
    void testIfStatementWithElif() {
        // TODO: refactor tests
        String s = "fn a() { if (true) { return 1; } elif(1 < 2) { return 2; } }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        var actualFunction = program.getFunctionDefinitions().get("a");

        Statement trueReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        Statement twoReturn = new ReturnStatement(new IntegerExpression(2, position), position);

        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position),
                        new LessExpression(new IntegerExpression(1, position), new IntegerExpression(2, position), position)),
                List.of(new BlockStatement(List.of(trueReturn), position),
                        new BlockStatement(List.of(twoReturn), position)),
                Optional.empty(),
                position
        );

        FunctionDefinition expected = new FunctionDefinition(
                "a",
                List.of(),
                Optional.empty(),
                new BlockStatement(List.of(ifStatement), position),
                position
        );

        assertEquals(expected.getName(), actualFunction.getName());
        assertEquals(expected.getParameters(), actualFunction.getParameters());
        assertEquals(expected.getReturnType(), actualFunction.getReturnType());

        BlockStatement expectedBlock = expected.getBlock();
        BlockStatement actualBlock = actualFunction.getBlock();
        assertEquals(expectedBlock, actualBlock);

        IfStatement expectedIf = (IfStatement) expectedBlock.getStatementList().get(0);
        IfStatement actualIf = (IfStatement) actualBlock.getStatementList().get(0);
        assertEquals(expectedIf, actualIf);

        assertEquals(expectedIf.getConditions(), actualIf.getConditions());
        assertEquals(expectedIf.getIfInstructions(), actualIf.getIfInstructions());
    }

    @Test
    void testIfStatementWithElifAndElse() {
        String sourceCode = "fn a() { if (true) { return 1; } elif(1 < 2) { return 2; } else { return 3; } }";
        FunctionDefinition actualFunction = parseAndBuildFunction(sourceCode);

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

        FunctionDefinition expectedFunction = new FunctionDefinition(
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
        String s = "fn a() { while (true) { return 1; } }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

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
        var fn = program.getFunctionDefinitions().get("a");
        assertEquals(new FunctionDefinition(
                        "a",
                        List.of(),
                        Optional.empty(),
                        new BlockStatement(
                                List.of(whileStatement),
                                position),
                        position),
                fn);
    }

    @Test
    void testAssignmentStatement() {
        String sourceCode = "fn a() { bool b = 0; abc = 1; }";
        FunctionDefinition actualFunction = parseAndBuildFunction(sourceCode);

        Statement declaratioinStatement = new DeclarationStatement(
                new Argument(new TypeDeclaration(ValueType.BOOL), "b"),
                new IntegerExpression(0, position),
                position
        );
        Statement assignmentStatement = new AssignmentStatement(
                "abc",
                new IntegerExpression(1, position),
                position
        );
        FunctionDefinition expectedFunction = new FunctionDefinition(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(declaratioinStatement, assignmentStatement),
                position
        );
        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testVariantDeclaration() {
        String sourceCode = "fn a() { Var v = Var::row(3); }";
        FunctionDefinition actualFunction = parseAndBuildFunction(sourceCode);

        Statement declaratioinStatement = new DeclarationStatement(
                new Argument(new TypeDeclaration(ValueType.CUSTOM, "Var"), "v"),
                new VariantExpression("row", new IntegerExpression(3, position), position),
                position
        );

        FunctionDefinition expectedFunction = new FunctionDefinition(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(declaratioinStatement),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }

    @Test
    void testVariantDefinition() {
        String sourceCode = "fn a() { Var v = Var::row(3); }";
        FunctionDefinition actualFunction = parseAndBuildFunction(sourceCode);
        Statement declaratioinStatement = new DeclarationStatement(
                new Argument(new TypeDeclaration(ValueType.CUSTOM, "Var"), "v"),
                new VariantExpression("row", new IntegerExpression(3, position), position),
                position
        );

        FunctionDefinition expectedFunction = new FunctionDefinition(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(declaratioinStatement),
                position
        );

        assertEquals(expectedFunction, actualFunction);
    }
}
