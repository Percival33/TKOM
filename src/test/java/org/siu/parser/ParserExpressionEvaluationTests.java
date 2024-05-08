package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Argument;
import org.siu.ast.Program;
import org.siu.ast.expression.CastedFactorExpression;
import org.siu.ast.expression.Expression;
import org.siu.ast.expression.FunctionCallExpression;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.type.*;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserExpressionEvaluationTests {
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

    private DeclarationStatement createDeclaration(String name, ValueType type, Expression expression) {
        Argument argument = new Argument(new TypeDeclaration(type), name);
        return new DeclarationStatement(argument, expression, position);
    }

    @Test
    void testNegateIntegerDeclaration() {
        String s = "int a=-1;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new NegateArithmeticExpression(new IntegerExpression(1, position), position);

        assertEquals(Map.of("a", createDeclaration("a", ValueType.INT, expression)), program.getDeclarations());
    }

    @Test
    void testCasteNegatedIntegerDeclaration() {
        String s = "int pi = ( int ) (-3.14159 + 0.0);";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new CastedFactorExpression(ValueType.INT, new AddArithmeticExpression(new NegateArithmeticExpression(new FloatExpression(3.14159F, position), position), new FloatExpression(0.0F, position), position), position);

        assertEquals(Map.of("pi", createDeclaration("pi", ValueType.INT, expression)), program.getDeclarations());
    }

    @Test
    void testMultiplyIntegersDeclaration() {
        String s = "int a = 2 * 1;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new MultiplyArithmeticExpression(new IntegerExpression(2, position), new IntegerExpression(1, position), position);

        assertEquals(Map.of("a", createDeclaration("a", ValueType.INT, expression)), program.getDeclarations());
    }

    @Test
    void testStringAssignmentDeclaration() {
        String s = "string c = \"a + b\";";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new StringExpression("a + b", position);
        assertEquals(createDeclaration("c", ValueType.STRING, expression), program.getDeclarations().get("c"));
    }

    @Test
    void testMultipleDeclaration() {
        String s = "int a = 2 % 1; float b = 3;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new ModuloArithmeticExpression(new IntegerExpression(2, position), new IntegerExpression(1, position), position);
        Expression secondExpression = new IntegerExpression(3, position);

        assertEquals(createDeclaration("a", ValueType.INT, expression), program.getDeclarations().get("a"));
        assertEquals(createDeclaration("b", ValueType.FLOAT, secondExpression), program.getDeclarations().get("b"));
    }

    @Test
    void testExpressionWithFnCallAndArithmeticOperations() {
        String s = "float a = 10 % f(33, 66) + 3;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        Expression right = new FunctionCallExpression("f", List.of(new IntegerExpression(33, position), new IntegerExpression(66, position)), position);
        Expression modulo = new ModuloArithmeticExpression(new IntegerExpression(10, position), right, position);
        Expression add = new AddArithmeticExpression(modulo, new IntegerExpression(3, position), position);

        assertEquals(createDeclaration("a", ValueType.FLOAT, add), program.getDeclarations().get("a"));
    }

    @Test
    void testOperatorPrecedenceDeclaration() {
        String s = "int b = 3 - 1 * 5 % 2 + 10;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        Expression multiply = new MultiplyArithmeticExpression(new IntegerExpression(1, position), new IntegerExpression(5, position), position);
        Expression modulo = new ModuloArithmeticExpression(multiply, new IntegerExpression(2, position), position);
        Expression subtract = new SubtractArithmeticExpression(new IntegerExpression(3, position), modulo, position);
        Expression add = new AddArithmeticExpression(subtract, new IntegerExpression(10, position), position);

        assertEquals(createDeclaration("b", ValueType.INT, add), program.getDeclarations().get("b"));
    }

    @Test
    void testBoolDeclaration() {
        String s = "bool b = false; bool c = true;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        Expression bExpression = new BooleanExpression(false, position);
        Expression cExpression = new BooleanExpression(true, position);

        assertEquals(createDeclaration("b", ValueType.BOOL, bExpression), program.getDeclarations().get("b"));
        assertEquals(createDeclaration("c", ValueType.BOOL, cExpression), program.getDeclarations().get("c"));
    }
}