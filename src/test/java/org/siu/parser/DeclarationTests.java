package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Parameter;
import org.siu.ast.Program;
import org.siu.ast.Statement;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.relation.GreaterExpression;
import org.siu.ast.statement.*;
import org.siu.ast.type.*;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeclarationTests {
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
        Parameter parameter = new Parameter(new TypeDeclaration(type), name);
        return new DeclarationStatement(parameter, expression, position);
    }

    private DeclarationStatement createDeclaration(String name, ValueType type, String typeName, Expression expression) {
        Parameter parameter = new Parameter(new TypeDeclaration(type, typeName), name);
        return new DeclarationStatement(parameter, expression, position);
    }

    @Test
    void testConstIntDeclaration() {
        String sourceCode = "const int a = 10;";
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();
        Parameter parameter = new Parameter(new TypeDeclaration(ValueType.INT), "a");
        ConstStatement expectedConst = new ConstStatement(
                parameter,
                new DeclarationStatement(parameter, new IntegerExpression(10, position), position),
                position
        );


        assertEquals(expectedConst, program.getDeclarations().get("a"));
    }

    @Test
    void testConstStringDeclaration() {
        String sourceCode = "const string a = \"aaaa\";";
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();
        Parameter parameter = new Parameter(new TypeDeclaration(ValueType.STRING), "a");
        ConstStatement expectedConst = new ConstStatement(
                parameter,
                new DeclarationStatement(parameter, new StringExpression("aaaa", position), position),
                position
        );


        assertEquals(expectedConst, program.getDeclarations().get("a"));
    }

    @Test
    void testConstStructDeclarationFromAnotherStruct() {
        String sourceCode = "const Point pt2 = pt;";
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();

        ConstStatement expectedConst = new ConstStatement(
                new Parameter(new TypeDeclaration(ValueType.CUSTOM, "Point"), "pt2"),
                createDeclaration("pt2", ValueType.CUSTOM, "Point", new IdentifierExpression("pt", position)),
                position
        );

        assertEquals(expectedConst, program.getDeclarations().get("pt2"));
    }

    @Test
    void testIntFromStructMember() {
        String sourceCode = "const int x = pt.x;";
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();

        ConstStatement expectedConst = new ConstStatement(
                new Parameter(new TypeDeclaration(ValueType.INT), "x"),
                createDeclaration("x", ValueType.INT, new StructExpression("pt", "x", position)),
                position
        );

        assertEquals(expectedConst, program.getDeclarations().get("x"));
    }

    @Test
    void testNegateIntegerDeclaration() {
        String s = "int a = -1;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new NegateArithmeticExpression(new IntegerExpression(1, position), position);

        assertEquals(Map.of("a", createDeclaration("a", ValueType.INT, expression)), program.getDeclarations());
    }

    @Test
    void testRelationExprAsValueOfIntegerDeclaration() {
        String s = "bool a = 1 > 2;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new GreaterExpression(new IntegerExpression(1, position), new IntegerExpression(2, position), position);

        assertEquals(createDeclaration("a", ValueType.BOOL, expression), program.getDeclarations().get("a"));
    }

    @Test
    void testFnCallAsValueOfIntegerDeclaration() {
        String s = "int a = -f(1);";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new NegateArithmeticExpression(new FunctionCallExpression("f", List.of(new IntegerExpression(1, position)), position), position);

        assertEquals(Map.of("a", createDeclaration("a", ValueType.INT, expression)), program.getDeclarations());
    }

    @Test
    void testCasteNegatedIntegerDeclaration() {
        String s = "int pi = ( int ) (-3.14159 + 0.0);";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new CastedFactorExpression(new TypeDeclaration(ValueType.INT), new AddArithmeticExpression(new NegateArithmeticExpression(new FloatExpression(3.14159F, position), position), new FloatExpression(0.0F, position), position), position);

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

    @Test
    void testConstInvalidDeclaration() {
        String sourceCode = "const  = 5;";
        Parser parser = toParser(sourceCode);

        assertThrows(RuntimeException.class, parser::buildProgram);
    }

    @Test
    void testStructDeclaration() {
        String sourceCode = "Point pt = { a, b, f() };";
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();
        DeclarationStatement expectedDeclaration = new DeclarationStatement(
                new Parameter(new TypeDeclaration(ValueType.CUSTOM, "Point"), "pt"),
                new StructDeclarationExpression(
                        List.of(
                                new IdentifierExpression("a", position),
                                new IdentifierExpression("b", position),
                                new FunctionCallExpression("f", List.of(), position)
                        ),
                        position
                ),
                position
        );

        assertEquals(expectedDeclaration, program.getDeclarations().get("pt"));
    }

    @Test
    void testStructDeclarationFromAnotherStruct() {
        String sourceCode = "Point pt2 = pt;";
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();

        assertEquals(createDeclaration("pt2", ValueType.CUSTOM, "Point", new IdentifierExpression("pt", position)), program.getDeclarations().get("pt2"));
    }

    @Test
    void testVariantDeclaration() {
        String sourceCode = "Var v = Var::row(3);";
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();

        Statement expectedDeclaration = createDeclaration(
                "v",
                ValueType.CUSTOM, "Var",
                new VariantDeclarationExpression("Var", "row", new IntegerExpression(3, position), position)
        );

        assertEquals(expectedDeclaration, program.getDeclarations().get("v"));
    }
}