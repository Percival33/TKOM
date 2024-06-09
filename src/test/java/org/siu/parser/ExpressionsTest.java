package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.BlockStatement;
import org.siu.ast.Parameter;
import org.siu.ast.Program;
import org.siu.ast.Statement;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.AddArithmeticExpression;
import org.siu.ast.expression.arithmetic.NegateArithmeticExpression;
import org.siu.ast.expression.arithmetic.SubtractArithmeticExpression;
import org.siu.ast.expression.logical.AndLogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.logical.OrLogicalExpression;
import org.siu.ast.expression.relation.LessExpression;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.type.*;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionsTest {
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

    private Program parseAndBuildProgram(String s) {
        Parser parser = toParser(s);
        return parser.buildProgram();
    }

    @Test
    void testAndLogicExpression() {
        Program program = parseAndBuildProgram("bool b = true;");

        Expression expression = new BooleanExpression(true, position);

        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testTwoAndLogicExpression() {
        Program program = parseAndBuildProgram("bool b = true or true;");

        Expression expression = new OrLogicalExpression(new BooleanExpression(true, position), new BooleanExpression(true, position), position);

        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testOrAndLogicExpression() {
        Program program = parseAndBuildProgram("bool b = true or false and true;");

        Expression expression = new OrLogicalExpression(new BooleanExpression(true, position), new AndLogicalExpression(new BooleanExpression(false, position), new BooleanExpression(true, position) , position) , position);

        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testOrAndAndLogicExpression() {
        Program program = parseAndBuildProgram("bool b = true or false and false and true;");

        Expression expression = new OrLogicalExpression(new BooleanExpression(true, position), new AndLogicalExpression(new BooleanExpression(false, position), new AndLogicalExpression(new BooleanExpression(false, position), new BooleanExpression(true, position) , position) , position) , position);

        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testNegateLogicalExpression() {
        Program program = parseAndBuildProgram("bool b = not true;");

        Expression expression = new NegateLogicalExpression(new BooleanExpression(true, position), position);

        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testRelationExpression() {
        Program program = parseAndBuildProgram("bool b = not true;");

        Expression expression = new NegateLogicalExpression(new BooleanExpression(true, position), position);

        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testMissingExpressionAfterNot() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> parseAndBuildProgram("bool b = not;"));
        assertTrue(exception.getMessage().trim().startsWith("Missing expression at the end of Position(line=1, column=6)"));
    }

    @Test
    void testRelationOperatorWithOutExpression() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> parseAndBuildProgram("bool b = 5 < ;"));
        assertTrue(exception.getMessage().trim().startsWith("org.siu.error.SyntaxError at: Position(line=1, column=12)"));
    }

    @Test
    void testMultipleRelationOperator() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> parseAndBuildProgram("bool b = 5 < 6 < 8;"));
        assertTrue(exception.getMessage().trim().startsWith("Missing semicolon at the end of the statement at: Position(line=1, column=16)"));
    }

    @Test
    void testRelationOperator() {
        Program program = parseAndBuildProgram("bool b = 5 < 6;");

        Expression expression = new LessExpression(new IntegerExpression(5, position), new IntegerExpression(6, position), position);
        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testAdditiveSubtractOperator() {
        Program program = parseAndBuildProgram("bool b = 5 < 3 - 6;");

        Expression expression = new LessExpression(new IntegerExpression(5, position), new SubtractArithmeticExpression(new IntegerExpression(3, position), new IntegerExpression(6, position), position), position);
        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testAdditiveAddOperator() {
        Program program = parseAndBuildProgram("bool b = 5 < 3 + 6 + 9;");

        Expression expression = new LessExpression(new IntegerExpression(5, position), new AddArithmeticExpression(new AddArithmeticExpression(new IntegerExpression(3, position), new IntegerExpression(6, position), position), new IntegerExpression(9, position),  position), position);
        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testCastedFactor() {
        Program program = parseAndBuildProgram("bool b = (int)1;");
        Expression expression = new CastedFactorExpression(new TypeDeclaration(ValueType.INT), new IntegerExpression(1, position), position);
        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testCastedInvalidTypeFactor() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> parseAndBuildProgram("bool b = (Point)1;"));
        assertEquals("Missing semicolon at the end of the statement at: Position(line=1, column=17)", thrown.getMessage());
    }

    @Test
    void testUnaryNegatedCastedFactor() {
        Program program = parseAndBuildProgram("bool b = (int)-1;");
        Expression expression = new CastedFactorExpression(new TypeDeclaration(ValueType.INT), new NegateArithmeticExpression(new IntegerExpression(1, position), position), position);
        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testStringLiteral() {
        Program program = parseAndBuildProgram("string b = \"AAda2da\";");
        Expression expression = new StringExpression("AAda2da", position);
        assertEquals(createDeclaration("b", ValueType.STRING, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testFloatLiteral() {
        Program program = parseAndBuildProgram("float b = 0.12345678;");
        Expression expression = new FloatExpression(0.1234567F, position); // Lexer will trim the float to 7 digits after the dot
        assertEquals(createDeclaration("b", ValueType.FLOAT, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testBoolLiteral() {
        Program program = parseAndBuildProgram("bool b = true;");
        Expression expression = new BooleanExpression(true, position);
        assertEquals(createDeclaration("b", ValueType.BOOL, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testExpressionInParenthesis() {
        Program program = parseAndBuildProgram("int b = (1 + 2);");
        Expression expression = new AddArithmeticExpression(new IntegerExpression(1, position), new IntegerExpression(2, position), position);
        assertEquals(createDeclaration("b", ValueType.INT, expression), program.getDeclarations().get("b"));
    }

    @Test
    void testIdentifierExpression() {
        Program program = parseAndBuildProgram("int p = x;");
        Expression expression = new IdentifierExpression("x", position);
        assertEquals(createDeclaration("p", ValueType.INT, expression), program.getDeclarations().get("p"));
    }

    @Test
    void testStructMemberExpression() {
        Program program = parseAndBuildProgram("int p = p.x;");
        Expression expression = new StructMemberExpression("p", "x", position);
        assertEquals(createDeclaration("p", ValueType.INT, expression), program.getDeclarations().get("p"));
    }

    @Test
    void testFnCallExpression() {
        Program program = parseAndBuildProgram("int p = a(1);");
        Expression expression = new FunctionCallExpression("a", List.of(new IntegerExpression(1, position)), position);
        assertEquals(createDeclaration("p", ValueType.INT, expression), program.getDeclarations().get("p"));
    }

    @Test
    void testVariantExpression() {
        Program program = parseAndBuildProgram("int p = Student::id(33);");
        Expression expression = new VariantDeclarationExpression("Student", "id", new IntegerExpression(33, position), position);
        assertEquals(createDeclaration("p", ValueType.INT, expression), program.getDeclarations().get("p"));
    }

    @Test
    void testStructExpression() {
        Program program = parseAndBuildProgram("Point p = Point { 4, 3 };");
        Expression expression = new StructDeclarationExpression("Point", List.of(new IntegerExpression(4, position), new IntegerExpression(3, position)), position);
        assertEquals(createDeclaration("p", ValueType.CUSTOM, "Point", expression), program.getDeclarations().get("p"));
    }

    @Test
    void testCopyExpression() {
        Program program = parseAndBuildProgram("fn foo() { f(@a); }");
        Statement statement = new FunctionCallExpression("f", List.of(new CopiedValueExpression(new IdentifierExpression("a", position), position)), position);
        assertEquals(statement, program.getFunctionDefinitions().get("foo").getBlock().getStatements().get(0));
    }
}
