package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Parameter;
import org.siu.ast.Program;
import org.siu.ast.expression.Expression;
import org.siu.ast.expression.IdentifierExpression;
import org.siu.ast.expression.logical.AndLogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.logical.OrLogicalExpression;
import org.siu.ast.expression.relation.EqualExpression;
import org.siu.ast.expression.relation.GreaterExpression;
import org.siu.ast.expression.relation.LessExpression;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.type.BooleanExpression;
import org.siu.ast.type.IntegerExpression;
import org.siu.ast.type.ValueType;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogicalExpressionTest {
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

    @Test
    void testAndLogicExpression() {
        String s = "bool b = true and false;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new AndLogicalExpression(new BooleanExpression(true, position), new BooleanExpression(false, position), position);

        assertEquals(Map.of("b", createDeclaration("b", ValueType.BOOL, expression)), program.getDeclarations());
    }

    @Test
    void testOrLogicExpression() {
        String s = "bool b = 5 or 5;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new OrLogicalExpression(new IntegerExpression(5, position), new IntegerExpression(5, position), position);

        assertEquals(Map.of("b", createDeclaration("b", ValueType.BOOL, expression)), program.getDeclarations());
    }

    @Test
    void testNegateLogicExpression() {
        String s = "bool b = not false;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new NegateLogicalExpression(new BooleanExpression(false, position), position);

        assertEquals(Map.of("b", createDeclaration("b", ValueType.BOOL, expression)), program.getDeclarations());
    }

    @Test
    void testNestedOrLogicExpression() {
        String s = "bool b = not (true or false);";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new NegateLogicalExpression(new OrLogicalExpression(new BooleanExpression(true, position), new BooleanExpression(false, position), position), position);

        assertEquals(Map.of("b", createDeclaration("b", ValueType.BOOL, expression)), program.getDeclarations());
    }

    @Test
    void testCombinedLogicExpression() {
        String s = "bool b = (x < 10 and y > 20) or (z == 0);";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();

        Expression expression = new OrLogicalExpression(
                new AndLogicalExpression(
                        new LessExpression(new IdentifierExpression("x", position), new IntegerExpression(10, position), position),
                        new GreaterExpression(new IdentifierExpression("y", position), new IntegerExpression(20, position), position),
                        position
                ),
                new EqualExpression(new IdentifierExpression("z", position), new IntegerExpression(0, position), position),
                position);
        assertEquals(Map.of("b", createDeclaration("b", ValueType.BOOL, expression)), program.getDeclarations());
    }
}
