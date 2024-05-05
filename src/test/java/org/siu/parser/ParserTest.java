package org.siu.parser;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Argument;
import org.siu.ast.Program;
import org.siu.ast.expression.FunctionCallExpression;
import org.siu.ast.expression.arithmetic.AddArithmeticExpression;
import org.siu.ast.expression.arithmetic.ModuloArithmeticExpression;
import org.siu.ast.expression.arithmetic.MultiplyArithmeticExpression;
import org.siu.ast.expression.arithmetic.SubtractArithmeticExpression;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.type.IntegerExpression;
import org.siu.ast.type.ValueType;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {
    private final ErrorHandler errorHandler = Mockito.mock(ErrorHandler.class);
    private final Position position = Mockito.mock(Position.class);

    Lexer toLexer(String text) {
        return new LexerImpl(text, errorHandler);
    }

    Parser toParser(String text) {
        Lexer lexer = toLexer(text);
        return new Parser(lexer, errorHandler);
    }

    @Test
    void addArithmeticOperation() throws Exception {
        String s = "int a = 2 * 1;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        assertEquals(Map.of("a", new DeclarationStatement(new Argument(ValueType.INT, "a"), new MultiplyArithmeticExpression(new IntegerExpression(2, position), new IntegerExpression(1, position), position), position)), program.getDeclarations());
    }

    @Test
    void moduloAddArithmeticExpressionsTest() throws Exception {
        String s = "int a = 10 % f(33, 66) + 3;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        assertEquals(new DeclarationStatement(
                        new Argument(ValueType.INT, "a"),
                        new AddArithmeticExpression(
                                new ModuloArithmeticExpression(
                                        new IntegerExpression(10, position),
                                        new FunctionCallExpression(
                                                "f",
                                                List.of(
                                                        new IntegerExpression(33, position),
                                                        new IntegerExpression(66, position)
                                                ),
                                                position
                                        ),
                                        position
                                ),
                                new IntegerExpression(3, position),
                                position
                        ),
                        position
                ),
                program.getDeclarations().get("a"));
    }

    @Test
    void ArithmeticExpressionsTest() {
        String s = "int b = 3 - 1 * 5 % 2 + 10;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        assertEquals(new DeclarationStatement(
                        new Argument(ValueType.INT, "b"),
                        new AddArithmeticExpression(
                                new SubtractArithmeticExpression(
                                        new IntegerExpression(3, position),
                                        new ModuloArithmeticExpression(
                                                new MultiplyArithmeticExpression(
                                                        new IntegerExpression(1, position),
                                                        new IntegerExpression(5, position),
                                                        position
                                                ),
                                                new IntegerExpression(2, position),
                                                position
                                        ),
                                        position
                                ),
                                new IntegerExpression(10, position),
                                position
                        ),
                        position
                ),
                program.getDeclarations().get("b"));
    }

    @Test
    public void a() {
//        "int a = 1 + 2 * 3;"              || new Program(Map.of(), Map.of("a", new DeclarationStatement(new Argument("a", new TypeDeclaration(ValueType.INTEGER)), new AddArithmeticExpression(new IntegerExpression(1, position), new MultiplyArithmeticExpression(new IntegerExpression(2, position), new IntegerExpression(3, position), position), position), position)))
    }
}