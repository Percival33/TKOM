package org.siu.interpreter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Program;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.parser.Parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class POCPrinterVisitorTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = Mockito.mock(ErrorHandler.class);
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    Program buildProgram(String text) {
        Lexer lexer = new LexerImpl(text, errorHandler);
        Parser parser = new Parser(lexer, errorHandler);
        return parser.buildProgram();
    }

    @Test
    void functionTest() {
        String code = "fn add(int a, int b): int { return a + b; }";
        Program program = buildProgram(code);
        var visitor = new PrinterVisitor(new PrintStream(outContent), program);
        visitor.execute();
        String expectedOutput = """
                FunctionDefinitionStatement
                 add -> TypeDeclaration(valueType=INT, customType=null)
                 Parameters
                  a -> TypeDeclaration(valueType=INT, customType=null)
                  b -> TypeDeclaration(valueType=INT, customType=null)
                 BlockStatement
                  Return Statement
                   AddArithmeticExpression
                    IdentifierExpression: a
                    IdentifierExpression: b
                """;
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void whileIfTest() {
        String code = """
                fn gcd(int a, int b): int {
                    while (a != b) {
                        if (a > b) {
                            a = a - b;
                        } else {
                            b = b - a;
                        }
                    }
                    return a;
                }
                """;
        Program program = buildProgram(code);
        var visitor = new PrinterVisitor(new PrintStream(outContent), program);
        visitor.execute();
        String expectedOutput = """
                FunctionDefinitionStatement
                 gcd -> TypeDeclaration(valueType=INT, customType=null)
                 Parameters
                  a -> TypeDeclaration(valueType=INT, customType=null)
                  b -> TypeDeclaration(valueType=INT, customType=null)
                 BlockStatement
                  While Statement
                   Condition
                   NotEqualExpression
                    IdentifierExpression: a
                    IdentifierExpression: b
                   BlockStatement
                    If Statement
                     Condition
                     GreaterExpression
                      IdentifierExpression: a
                      IdentifierExpression: b
                     BlockStatement
                      AssignmentStatement: a
                       SubtractArithmeticExpression
                        IdentifierExpression: a
                        IdentifierExpression: b
                     Else Block
                     BlockStatement
                      AssignmentStatement: b
                       SubtractArithmeticExpression
                        IdentifierExpression: b
                        IdentifierExpression: a
                 Return Statement
                  IdentifierExpression: a
                """;
        assertEquals(expectedOutput, outContent.toString());
    }
}
