package org.siu.parser;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Argument;
import org.siu.ast.Program;
import org.siu.ast.expression.arithmetic.AddArithmeticExpression;
import org.siu.ast.expression.arithmetic.ModuloArithmeticExpression;
import org.siu.ast.expression.arithmetic.MultiplyArithmeticExpression;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.type.IntegerExpression;
import org.siu.ast.type.ValueType;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

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
    void addOperatorTest() throws Exception {
        String s = "int a = 2 * 1;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        assertEquals(program.getDeclarations(), Map.of("a", new DeclarationStatement(new Argument(ValueType.INT, "a"), new MultiplyArithmeticExpression(new IntegerExpression(2, position), new IntegerExpression(1, position), position), position)));
    }

    @Test
    void arithmeticOperatorTest() throws Exception {
        String s = "int a = 10 % 2 + 3;";
        Parser parser = toParser(s);
        Program program = parser.buildProgram();
        DeclarationStatement a = program.getDeclarations().get("a");
        DeclarationStatement expected = new DeclarationStatement(
                new Argument(ValueType.INT, "a"),
                new AddArithmeticExpression(
                        new ModuloArithmeticExpression(
                                new IntegerExpression(10, position),
                                new IntegerExpression(2, position),
                                position
                        ),
                        new IntegerExpression(3, position),
                        position
                ),
                new Position(1,5)
        );

        assertEquals(a, expected);
//        assertEquals(a,

    }

    @Test
    public void a() {
//        "int a = 1 + 2 * 3;"              || new Program(Map.of(), Map.of("a", new DeclarationStatement(new Argument("a", new TypeDeclaration(ValueType.INTEGER)), new AddArithmeticExpression(new IntegerExpression(1, position), new MultiplyArithmeticExpression(new IntegerExpression(2, position), new IntegerExpression(3, position), position), position), position)))
    }
}