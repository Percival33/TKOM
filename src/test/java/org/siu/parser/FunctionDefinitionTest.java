package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Parameter;
import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.Statement;
import org.siu.ast.expression.FunctionCallExpression;
import org.siu.ast.expression.IdentifierExpression;
import org.siu.ast.expression.StructDeclarationExpression;
import org.siu.ast.expression.arithmetic.AddArithmeticExpression;
import org.siu.ast.expression.arithmetic.MultiplyArithmeticExpression;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.statement.AssignmentStatement;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.statement.IfStatement;
import org.siu.ast.statement.ReturnStatement;
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

    private Program parseAndBuildProgram(String s) {
        Parser parser = toParser(s);
        return parser.buildProgram();
    }

    private BlockStatement blockOf(Statement... statements) {
        return new BlockStatement(List.of(statements), position);
    }

    @Test
    void fnDefinitionTest() {
        String s = "fn add(int a, int b): int { return a + b; }";
        var fn = parseAndBuildProgram(s).getFunctionDefinitions().get("add");

        BlockStatement block = blockOf(new ReturnStatement(
                new AddArithmeticExpression(
                        new IdentifierExpression("a", position),
                        new IdentifierExpression("b", position),
                        position
                ),
                position));

        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "add",
                List.of(new Parameter(new TypeDeclaration(ValueType.INT), "a"), new Parameter(new TypeDeclaration(ValueType.INT), "b")),
                Optional.of(new TypeDeclaration(ValueType.INT)),
                block,
                position
        );

        assertEquals(expected, fn);
    }

    @Test
    void fnDefinitionWithNoReturnValue() {
        String s = "fn add(int a) {}";
        var fn = parseAndBuildProgram(s).getFunctionDefinitions().get("add");

        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "add",
                List.of(new Parameter(new TypeDeclaration(ValueType.INT), "a")),
                Optional.empty(),
                new BlockStatement(List.of(), position),
                position
        );

        assertEquals(expected, fn);
    }

    @Test
    void fnDefinitionWithNoArguments() {
        String s = "fn add() { f(1); }";
        var fn = parseAndBuildProgram(s).getFunctionDefinitions().get("add");

        BlockStatement block = blockOf(new FunctionCallExpression("f", List.of(new IntegerExpression(1, position)), position));

        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "add",
                List.of(),
                Optional.empty(),
                block,
                position
        );

        assertEquals(expected, fn);
    }

    @Test
    void fnDefinitionTest2() {
        String s = "fn fun(int a, int b): float { int c = 5; return a + b * c; }";
        var fn = parseAndBuildProgram(s).getFunctionDefinitions().get("fun");

        BlockStatement block = getBlockStatement();

        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "fun",
                List.of(new Parameter(new TypeDeclaration(ValueType.INT), "a"), new Parameter(new TypeDeclaration(ValueType.INT), "b")),
                Optional.of(new TypeDeclaration(ValueType.FLOAT)),
                block,
                position
        );

        assertEquals(expected, fn);
    }

    @Test
    void fnDefinitionWithCustomTypeReturnTest() {
        String s = "fn fun(int a): Point { return Point { 1, 2 }; }";
        var fn = parseAndBuildProgram(s).getFunctionDefinitions().get("fun");

        BlockStatement block = blockOf(new ReturnStatement(
                new StructDeclarationExpression("Point", List.of(new IntegerExpression(1, position), new IntegerExpression(2, position)), position),
                position));

        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "fun",
                List.of(new Parameter(new TypeDeclaration(ValueType.INT), "a")),
                Optional.of(new TypeDeclaration(ValueType.CUSTOM, "Point")),
                block,
                position
        );

        assertEquals(expected, fn);
    }

    @Test
    void fnDefinitionCustomTypeParameterTest() {
        String s = "fn fun(Point p): Point { p = Point { 1, 2 }; return p; }";
        var fn = parseAndBuildProgram(s).getFunctionDefinitions().get("fun");

        BlockStatement block = blockOf(new AssignmentStatement(
                        "p",
                        new StructDeclarationExpression("Point", List.of(new IntegerExpression(1, position), new IntegerExpression(2, position)), position),
                        position
                ),
                new ReturnStatement(
                        new IdentifierExpression("p", position),
                        position
                ));

        FunctionDefinitionStatement expected = new FunctionDefinitionStatement(
                "fun",
                List.of(new Parameter(new TypeDeclaration(ValueType.CUSTOM, "Point"), "p")),
                Optional.of(new TypeDeclaration(ValueType.CUSTOM, "Point")),
                block,
                position
        );

        assertEquals(expected, fn);
    }

    private BlockStatement getBlockStatement() {
        Statement declarationStatement = new DeclarationStatement(
                new Parameter(new TypeDeclaration(ValueType.INT), "c"),
                new IntegerExpression(5, position),
                position
        );
        Statement returnStatement = new ReturnStatement(
                new AddArithmeticExpression(
                        new IdentifierExpression("a", position),
                        new MultiplyArithmeticExpression(
                                new IdentifierExpression("b", position),
                                new IdentifierExpression("c", position),
                                position
                        ),
                        position
                ),
                position
        );

        return blockOf(declarationStatement, returnStatement);
    }

    @Test
    void testInvalidSemicolon() {
        Program program = parseAndBuildProgram("fn foo() {};");
        FunctionDefinitionStatement statement = new FunctionDefinitionStatement("foo", List.of(), Optional.empty(), new BlockStatement(List.of(), position), position);
        assertEquals(statement, program.getFunctionDefinitions().get("foo"));
    }

    @Test
    void testReturnValueFromIntFunction() {
        Program program = parseAndBuildProgram("""
                fn foo(): int {
                    if(true) {
                        return 1;
                    }
                }
                """);
        FunctionDefinitionStatement statement = new FunctionDefinitionStatement("foo", List.of(), Optional.of(new TypeDeclaration(ValueType.INT)), new BlockStatement(List.of(
                new IfStatement(
                        List.of(new BooleanExpression(true, position)),
                        List.of(blockOf(new ReturnStatement(new IntegerExpression(1, position), position))),
                        Optional.empty(),
                        position
                )
        ), position), position);

        assertEquals(statement, program.getFunctionDefinitions().get("foo"));
    }
}
