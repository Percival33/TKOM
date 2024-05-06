package org.siu.lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.siu.error.ErrorHandler;
import org.siu.token.Position;
import org.siu.token.TokenType;
import org.siu.token.type.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class LexerImplTest {
    private final ErrorHandler errorHandler = Mockito.mock(ErrorHandler.class);

    Lexer setup(String text) {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        return new LexerImpl(reader, errorHandler);
    }

    @ParameterizedTest
    @ValueSource(ints = {123, 0, Integer.MAX_VALUE, Integer.MAX_VALUE})
    void testInteger(int expected) {
        Lexer lexer = setup(String.valueOf(expected));
        assertEquals(new IntegerToken(new Position(1, 1), expected), lexer.nextToken());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void boolValue(boolean value) {
        Lexer lexer = setup(String.valueOf(value));
        assertEquals(new BooleanToken(new Position(1, 1), value), lexer.nextToken());
    }

    @ParameterizedTest
    @ValueSource(floats = {1.1F, 1.0001F})
    void parseFloat(float value) {
        Lexer lexer = setup(String.valueOf(value));
        var returnedToken = lexer.nextToken();
        assertEquals(TokenType.FLOAT_CONSTANT, returnedToken.getType());
        assertEquals(Float.valueOf(value), returnedToken.getValue());
    }

    @Test
    void tooLongFloat() {
        Lexer lexer = setup("1." + "0".repeat(LexerConfig.MAX_FRACTIONAL_DIGITS) + "1");
        assertEquals(new FloatToken(new Position(1, 1), 1.0F), lexer.nextToken());
        Mockito.verify(errorHandler).handleLexerError(Mockito.any(Exception.class), Mockito.any(Position.class));
    }


    static Stream<Arguments> stringProvider() {
        return Stream.of(
                arguments("\"Ala nie ma kota.\"", "Ala nie ma kota."),
                arguments("\"Niesamowita \\\" sprawa.\"", "Niesamowita \" sprawa."),
                arguments("\"Ala nie\n ma kota.\"", """
                        Ala nie
                         ma kota."""),
                arguments("\"Ala nie ma \t kota.\"", """
                        Ala nie ma \t kota."""),
                arguments("\"\t'\\\"\\n\\\"\\a\"", "\t'\"\n\"\\a")
        );
    }

    @ParameterizedTest
    @MethodSource("stringProvider")
    void testString(String input, String expected) {
        Lexer lexer = setup(input);
        var returnedToken = lexer.nextToken();
        assertEquals(TokenType.STRING_CONSTANT, returnedToken.getType());
        assertEquals(expected, returnedToken.getValue());
    }

    @Test
    void returnsOnlyEOFAtTheEndOfInput() {
        Lexer lexer = setup("");
        assertEquals(TokenType.END_OF_FILE, lexer.nextToken().getType());
        assertEquals(TokenType.END_OF_FILE, lexer.nextToken().getType());
    }

    @Test
    void stringRegressionTest() {
        Lexer lexer = setup("string c = \"a + b\";");
        assertEquals(TokenType.STRING, lexer.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().getType());
        assertEquals(TokenType.ASSIGN, lexer.nextToken().getType());
        var token = lexer.nextToken();
        assertEquals("a + b", token.getValue());
        assertEquals(TokenType.STRING_CONSTANT, token.getType());
        assertEquals(TokenType.SEMICOLON, lexer.nextToken().getType());
        assertEquals(TokenType.END_OF_FILE, lexer.nextToken().getType());
    }

    @ParameterizedTest
    @CsvSource({
            "'<=', 'LESS_EQUAL'",
            "'<', 'LESS'",
            "'==', 'COMPARE_EQUAL'",
            "'!=', 'COMPARE_NOT_EQUAL'",
            "'=', 'ASSIGN'",
            "'and', 'AND'",
            "'or', 'OR'",
            "'not', 'NOT'",
    })
    void testOperators(String input, String expected) {
        Lexer lexer = setup(input);
        assertEquals(expected, lexer.nextToken().getType().name());
    }

    @Test
    void tooLongIdentifier() {
        Lexer lexer = setup("a".repeat(LexerConfig.MAX_IDENTIFIER_LENGTH + 10));
        var token = lexer.nextToken();
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 1), "a".repeat(LexerConfig.MAX_IDENTIFIER_LENGTH)), token);
        Mockito.verify(errorHandler).handleLexerError(Mockito.any(Exception.class), Mockito.any(Position.class));
    }

    @Test
    void negativeIntDeclaration() {
        Lexer lexer = setup("int x =-10\n;");
        assertEquals(new KeywordToken(TokenType.INT, new Position(1, 1)), lexer.nextToken(), "Expected keyword 'INT'");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 5), "x"), lexer.nextToken(), "Expected identifier");
        assertEquals(new KeywordToken(TokenType.ASSIGN, new Position(1, 7)), lexer.nextToken(), "Expected ASSIGN");
        assertEquals(new KeywordToken(TokenType.MINUS, new Position(1, 8)), lexer.nextToken(), "Expected minus symbol");
        assertEquals(new IntegerToken(new Position(1, 9), 10), lexer.nextToken(), "Expected integer token");
        assertEquals(new KeywordToken(TokenType.SEMICOLON, new Position(2, 1)), lexer.nextToken(), "Expected semicolon");
        assertEquals(new KeywordToken(TokenType.END_OF_FILE, new Position(2, 2)), lexer.nextToken(), "Expected end of file token");
        assertEquals(new KeywordToken(TokenType.END_OF_FILE, new Position(2, 2)), lexer.nextToken(), "Expected end of file token");
        assertEquals(new KeywordToken(TokenType.END_OF_FILE, new Position(2, 2)), lexer.nextToken(), "Expected end of file token");
    }

    @Test
    void singleLineComment() {
        Lexer lexer = setup(" # \n aaa");
        assertEquals(new KeywordToken(TokenType.SINGLE_LINE_COMMENT, new Position(1, 2)), lexer.nextToken(), "Expected single line comment");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(2, 2), "aaa"), lexer.nextToken(), "Expected identifier");
    }

    @Test
    void multiLineComment() {
        Lexer lexer = setup(" /*\naaa\n*/\nbbb");
        assertEquals(new KeywordToken(TokenType.MULTI_LINE_COMMENT_OPEN, new Position(1, 2)), lexer.nextToken(), "Expected single line comment");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(2, 1), "aaa"), lexer.nextToken(), "Expected identifier");
        assertEquals(new KeywordToken(TokenType.MULTI_LINE_COMMENT_CLOSE, new Position(3, 1)), lexer.nextToken(), "Expected single line comment");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(4, 1), "bbb"), lexer.nextToken(), "Expected identifier");
    }
}