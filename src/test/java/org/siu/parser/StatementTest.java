package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Argument;
import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.Statement;
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
        String s = "fn a() { if (true) { return 1; } elif(1 < 2) { return 2; } }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        // Parse the actual function from the program
        var actualFunction = program.getFunctionDefinitions().get("a");

        // Expected structures
        Statement trueReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        Statement twoReturn = new ReturnStatement(new IntegerExpression(2, position), position);

        // Constructing the expected IfStatement
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

        // Checking individual elements
        assertEquals(expected.getName(), actualFunction.getName());
        assertEquals(expected.getParameters(), actualFunction.getParameters());
        assertEquals(expected.getReturnType(), actualFunction.getReturnType());

        // Check top-level block
        BlockStatement expectedBlock = expected.getBlock();
        BlockStatement actualBlock = actualFunction.getBlock();
        assertEquals(expectedBlock, actualBlock);

        // Check IfStatement
        IfStatement expectedIf = (IfStatement) expectedBlock.getStatementList().get(0);
        IfStatement actualIf = (IfStatement) actualBlock.getStatementList().get(0);
        assertEquals(expectedIf, actualIf);

        // Check conditions and blocks within IfStatement
        assertEquals(expectedIf.getConditions(), actualIf.getConditions());
        assertEquals(expectedIf.getIfInstructions(), actualIf.getIfInstructions());
    }

    @Test
    void testIfStatementWithElifAndElse() {
        String s = "fn a() { if (true) { return 1; } elif(1 < 2) { return 2; } else { return 3; } }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        // Parse the actual function from the program
        var actualFunction = program.getFunctionDefinitions().get("a");

        // Expected structures
        Statement trueReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        Statement twoReturn = new ReturnStatement(new IntegerExpression(2, position), position);
        Statement elseReturn = new ReturnStatement(new IntegerExpression(3, position), position);
        BlockStatement elseBlock = new BlockStatement(List.of(elseReturn), position);
        // Constructing the expected IfStatement
        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position),
                        new LessExpression(new IntegerExpression(1, position), new IntegerExpression(2, position), position)),
                List.of(new BlockStatement(List.of(trueReturn), position),
                        new BlockStatement(List.of(twoReturn), position)),
                Optional.of(elseBlock),
                position
        );

        FunctionDefinition expected = new FunctionDefinition(
                "a",
                List.of(),
                Optional.empty(),
                new BlockStatement(List.of(ifStatement), position),
                position
        );

        // Checking individual elements
        assertEquals(expected.getName(), actualFunction.getName());
        assertEquals(expected.getParameters(), actualFunction.getParameters());
        assertEquals(expected.getReturnType(), actualFunction.getReturnType());

        // Check top-level block
        BlockStatement expectedBlock = expected.getBlock();
        BlockStatement actualBlock = actualFunction.getBlock();
        assertEquals(expectedBlock, actualBlock);

        // Check IfStatement
        IfStatement expectedIf = (IfStatement) expectedBlock.getStatementList().get(0);
        IfStatement actualIf = (IfStatement) actualBlock.getStatementList().get(0);
        assertEquals(expectedIf, actualIf);

        // Check conditions and blocks within IfStatement
        assertEquals(expectedIf.getConditions(), actualIf.getConditions());
        assertEquals(expectedIf.getIfInstructions(), actualIf.getIfInstructions());

        // Additional check for else block
        assertTrue(expectedIf.getElseInstructions().isPresent(), "Expected else block is missing");
        assertTrue(actualIf.getElseInstructions().isPresent(), "Actual else block is missing");
        assertEquals(expectedIf.getElseInstructions().get(), actualIf.getElseInstructions().get());
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
        String s = "fn a() { bool b = 0; abc = 1; }";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
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
        var fn = program.getFunctionDefinitions().get("a");
        assertEquals(new FunctionDefinition(
                        "a",
                        List.of(),
                        Optional.empty(),
                        new BlockStatement(
                                List.of(declaratioinStatement, assignmentStatement),
                                position),
                        position),
                fn);
    }
}
