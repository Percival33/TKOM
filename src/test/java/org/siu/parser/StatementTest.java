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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        FunctionDefinitionStatement expectedFunction = createIfStatementFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createIfStatementFunction() {
        Statement oneReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position)),
                List.of(new BlockStatement(List.of(oneReturn), position)),
                Optional.empty(),
                position
        );

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                new BlockStatement(List.of(ifStatement), position),
                position
        );
    }

    @Test
    void testIfStatementWithElse() {
        String sourceCode = "fn a() { if (true) { return 1; } else { return 2; } }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createIfStatementWithElseFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createIfStatementWithElseFunction() {
        Statement oneReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        BlockStatement elseBlock = new BlockStatement(List.of(new ReturnStatement(new IntegerExpression(2, position), position)), position);

        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position)),
                List.of(new BlockStatement(List.of(oneReturn), position)),
                Optional.of(elseBlock),
                position
        );

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                new BlockStatement(List.of(ifStatement), position),
                position
        );
    }

    @Test
    void testIfStatementWithTwoElseBlocks() {
        String sourceCode = "fn a() { \nif (true) {\n return 1; \n} else { \nreturn 2;\n } else {\n return 3; \n} }";

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> parseAndBuildFunction(sourceCode));
        assertEquals("org.siu.error.SyntaxError at: Position(line=6, column=4)", thrown.getMessage());
    }

    @Test
    void testIfStatementWithElif() {
        String sourceCode = "fn a() { if (true) { return 1; } elif(1 < 2) { return 2; } }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createIfStatementWithElifFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createIfStatementWithElifFunction() {
        Statement trueReturn = new ReturnStatement(new IntegerExpression(1, position), position);
        Statement twoReturn = new ReturnStatement(new IntegerExpression(2, position), position);

        IfStatement ifStatement = new IfStatement(
                List.of(new BooleanExpression(true, position), new LessExpression(new IntegerExpression(1, position), new IntegerExpression(2, position), position)),
                List.of(new BlockStatement(List.of(trueReturn), position), new BlockStatement(List.of(twoReturn), position)),
                Optional.empty(),
                position
        );

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                new BlockStatement(List.of(ifStatement), position),
                position
        );
    }

    @Test
    void testIfStatementWithElifAndElse() {
        String sourceCode = "fn a() { if (true) { return 1; } elif(1 < 2) { return 2; } else { return 3; } }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createIfStatementWithElifAndElseFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createIfStatementWithElifAndElseFunction() {
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

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(ifStatement),
                position
        );
    }

    @Test
    void testIFStatementWithNoBlock() {
        String sourceCode = "fn a() { \nif (true) }";

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> parseAndBuildFunction(sourceCode));
        assertEquals("org.siu.error.MissingBlockStatementException at: Position(line=2, column=11)", thrown.getMessage());
    }

    @Test
    void testIFStatementWithCondition() {
        String sourceCode = "fn a() { \nif {} }";

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> parseAndBuildFunction(sourceCode));
        assertEquals("org.siu.error.InvalidConditionExpressionException at: Position(line=2, column=4)", thrown.getMessage());
    }

    @Test
    void testIFStatementWithNotClosedCondition() {
        String sourceCode = "fn a() { \nif(true {} }";

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> parseAndBuildFunction(sourceCode));
        assertEquals("org.siu.error.InvalidConditionExpressionException at: Position(line=2, column=9)", thrown.getMessage());
    }

    @Test
    void testWhileStatementWithNoBlock() {
        String sourceCode = "fn a() { \nwhile (true) }";

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> parseAndBuildFunction(sourceCode));
        assertEquals("org.siu.error.MissingBlockStatementException at: Position(line=2, column=14)", thrown.getMessage());
    }

    @Test
    void testWhileStatement() {
        String sourceCode = "fn a() { while (true) { return 1; } }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createWhileStatementFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createWhileStatementFunction() {
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

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(whileStatement),
                position
        );
    }

    @Test
    void testAssignmentStatement() {
        String sourceCode = "fn a() { bool b = 0; abc = 1; }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createAssignmentStatementFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createAssignmentStatementFunction() {
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
        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(declaratioinStatement, assignmentStatement),
                position
        );
    }

    @Test
    void testCopyValueStatement() {
        String sourceCode = "fn a() { f(@abc); }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createCopyValueStatementFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createCopyValueStatementFunction() {
        Expression argument = new CopiedValueExpression(
                new IdentifierExpression("abc", position),
                position
        );
        Statement functionCall = new FunctionCallExpression("f", List.of(argument), position);

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(functionCall),
                position
        );
    }

    @Test
    void testVariantDeclaration() {
        String sourceCode = "fn a() { Var v = Var::row(3); }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createVariantDeclarationFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createVariantDeclarationFunction() {
        Statement declaratioinStatement = new DeclarationStatement(
                new Parameter(new TypeDeclaration(ValueType.CUSTOM, "Var"), "v"),
                new VariantDeclarationExpression("Var", "row", new IntegerExpression(3, position), position),
                position
        );

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(declaratioinStatement),
                position
        );
    }

    @Test
    void testInvalidMatchStatement() {
        String sourceCode = """
                fn a(Var v): Var {
                    match(v) => {
                        Var::row(x) { return x; }
                        Var::col(y) { return y; }
                    }
                    return value;
                }
                """;
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> parseAndBuildFunction(sourceCode));
        assertEquals("org.siu.error.SyntaxError at: Position(line=2, column=14)", thrown.getMessage());
    }

    @Test
    void testMatchStatement() {
        String sourceCode = """
                fn a(Var v): Var {
                    match(v) {
                        Var::row(x) { return x; }
                        Var::col(y) { return y; }
                    }
                    return value;
                }
                """;
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createMatchStatementFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createMatchStatementFunction() {
        MatchCaseStatement rowCase = new MatchCaseStatement(
                "Var",
                "row",
                "x",
                blockOf(new ReturnStatement(new IdentifierExpression("x", position), position)),
                position
        );
        MatchCaseStatement colCase = new MatchCaseStatement(
                "Var",
                "col",
                "y",
                blockOf(new ReturnStatement(new IdentifierExpression("y", position), position)),
                position
        );
        Statement matchStatement = new MatchStatement(new IdentifierExpression("v", position), List.of(rowCase, colCase), position);
        Statement returnStatement = new ReturnStatement(new IdentifierExpression("value", position), position);

        return new FunctionDefinitionStatement(
                "a",
                List.of(new Parameter(new TypeDeclaration(ValueType.CUSTOM, "Var"), "v")),
                Optional.of(new TypeDeclaration(ValueType.CUSTOM, "Var")),
                blockOf(matchStatement, returnStatement),
                position
        );
    }

    @Test
    void testStructMemberAssignment() {
        String sourceCode = "fn a() { pluto.age = 3; }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createStructMemberAssignmentFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createStructMemberAssignmentFunction() {
        Statement assignmentStatement = new StructMemberAssignmentStatement(
                new StructMemberExpression("pluto", "age", position),
                new IntegerExpression(3, position),
                position
        );

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(assignmentStatement),
                position
        );
    }

    @Test
    void returnStatementTest() {
        String sourceCode = "fn a() { return 1; }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createReturnStatementFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createReturnStatementFunction() {
        Statement returnStatement = new ReturnStatement(new IntegerExpression(1, position), position);

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(returnStatement),
                position
        );
    }

    @Test
    void emptyReturnStatementTest() {
        String sourceCode = "fn a() { return; }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createEmptyReturnStatment();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createEmptyReturnStatment() {
        Statement returnStatement = new ReturnStatement(null, position);

        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(returnStatement),
                position
        );
    }

    @Test
    void emptyBlockTest() {
        String sourceCode = "fn a() {}";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createEmptyBlockFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createEmptyBlockFunction() {
        return new FunctionDefinitionStatement(
                "a",
                List.of(),
                Optional.empty(),
                blockOf(),
                position
        );
    }

    @Test
    void multipleStatementsInblockTest() {
        String sourceCode = "fn a() { int a = 1; a = 2; return a; }";
        FunctionDefinitionStatement actualFunction = parseAndBuildFunction(sourceCode);
        FunctionDefinitionStatement expectedFunction = createMultipleStatementsInBlockFunction();

        assertEquals(expectedFunction, actualFunction);
    }

    private FunctionDefinitionStatement createMultipleStatementsInBlockFunction() {
        DeclarationStatement declarationStatement = new DeclarationStatement(
                new Parameter(new TypeDeclaration(ValueType.INT), "a"),
                new IntegerExpression(1, position),
                position
        );

        AssignmentStatement assignmentStatement = new AssignmentStatement(
                "a",
                new IntegerExpression(2, position),
                position
        );

        ReturnStatement returnStatement = new ReturnStatement(new IdentifierExpression("a", position), position);

        return new FunctionDefinitionStatement("a", List.of(), Optional.empty(), blockOf(declarationStatement, assignmentStatement, returnStatement), position);
    }
}
