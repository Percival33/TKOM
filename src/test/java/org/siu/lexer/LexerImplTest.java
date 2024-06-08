package org.siu.lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.siu.error.ErrorHandler;
import org.siu.error.InvalidTokenException;
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
    @ValueSource(ints = {123, 0, Integer.MAX_VALUE})
    void testInteger(int expected) {
        Lexer lexer = setup(String.valueOf(expected));
        assertEquals(new IntegerToken(new Position(1, 1), expected), lexer.nextToken());
    }

    @Test
    void testMinInt() {
        Lexer lexer = setup(String.valueOf(Integer.MIN_VALUE + 1));
        assertEquals(new KeywordToken(TokenType.MINUS, new Position(1, 1)), lexer.nextToken());
        assertEquals(new IntegerToken(new Position(1, 2), Integer.MAX_VALUE), lexer.nextToken());
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
    void minFloat() {
        Lexer lexer = setup("0." + "0".repeat(6) + "1");
        assertEquals(new FloatToken(new Position(1, 1), (float) 1.0E-7), lexer.nextToken());
    }

    @Test
    void tooLongFloat() {
        Lexer lexer = setup("1." + "0".repeat(LexerConfig.MAX_FRACTIONAL_DIGITS) + "1");
        assertEquals(new FloatToken(new Position(1, 1), 1.0F), lexer.nextToken());
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
            "'==', 'EQUAL'",
            "'!=', 'NOT_EQUAL'",
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
        Lexer lexer = setup(" #  aaa");
        assertEquals(new CommentToken(new Position(1, 2), " aaa"), lexer.nextToken(), "Expected single line comment");
    }

    @Test
    void breakCommand() {
        Lexer lexer = setup("break");
        assertEquals(new KeywordToken(TokenType.BREAK, new Position(1, 1)), lexer.nextToken(), "Expected single line comment");
    }

    @Test
    void continueCommand() {
        Lexer lexer = setup("continue");
        assertEquals(new KeywordToken(TokenType.CONTINUE, new Position(1, 1)), lexer.nextToken(), "Expected single line comment");
    }

    @Test
    void multiLineComment() {
        Lexer lexer = setup(" /*\naaa\n* /\nbbb */");
        assertEquals(new CommentToken(new Position(1, 2), "\naaa\n* /\nbbb "), lexer.nextToken(), "Expected multi line comment");
    }

    @Test
    void fnDeclaration() {
        Lexer lexer = setup("fn add(int a, int b): int { # bleble \n return a + b; }");
        assertEquals(new KeywordToken(TokenType.FUNCTION, new Position(1, 1)), lexer.nextToken(), "Expected FUNCTION");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 4), "add"), lexer.nextToken(), "Expected identifier");
        assertEquals(new KeywordToken(TokenType.BRACKET_OPEN, new Position(1, 7)), lexer.nextToken(), "Expected BRACKET_OPEN");
        assertEquals(new KeywordToken(TokenType.INT, new Position(1, 8)), lexer.nextToken(), "Expected INT");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 12), "a"), lexer.nextToken(), "Expected identifier");
        assertEquals(new KeywordToken(TokenType.COMMA, new Position(1, 13)), lexer.nextToken(), "Expected COMMA");
        assertEquals(new KeywordToken(TokenType.INT, new Position(1, 15)), lexer.nextToken(), "Expected INT");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 19), "b"), lexer.nextToken(), "Expected identifier");
        assertEquals(new KeywordToken(TokenType.BRACKET_CLOSE, new Position(1, 20)), lexer.nextToken(), "Expected BRACKET_CLOSE");
        assertEquals(new KeywordToken(TokenType.COLON, new Position(1, 21)), lexer.nextToken(), "Expected COLON");
        assertEquals(new KeywordToken(TokenType.INT, new Position(1, 23)), lexer.nextToken(), "Expected INT");
        assertEquals(new KeywordToken(TokenType.CURLY_BRACKET_OPEN, new Position(1, 27)), lexer.nextToken(), "Expected SQUARE_BRACKET_OPEN");
        assertEquals(new CommentToken(new Position(1, 29), "bleble \n"), lexer.nextToken(), "Expected comment");
        assertEquals(new KeywordToken(TokenType.RETURN, new Position(2, 2)), lexer.nextToken(), "Expected RETURN");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(2, 9), "a"), lexer.nextToken(), "Expected identifier");
        assertEquals(new KeywordToken(TokenType.PLUS, new Position(2, 11)), lexer.nextToken(), "Expected PLUS");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(2, 13), "b"), lexer.nextToken(), "Expected identifier");
        assertEquals(new KeywordToken(TokenType.SEMICOLON, new Position(2, 14)), lexer.nextToken(), "Expected SEMICOLON");
        assertEquals(new KeywordToken(TokenType.CURLY_BRACKET_CLOSE, new Position(2, 16)), lexer.nextToken(), "Expected SQUARE_BRACKET_CLOSE");
    }

    @Test
    void testInvalidLexerError() {
        Lexer lexer = setup("~!@#$%^&*()?\"}{:>L<KMJHGFVCDSES$ERTGHPNO\"m;\n l,';l|_)(*&^%R$EW HŻA#S$XRC&^F^*VBIH)*&^F*TR%$#@!~!@#$%^&*INBUVF^D%");
        assertThrows(InvalidTokenException.class, lexer::nextToken);
    }

    @Test
    void testStructTypeDeclaration() {
        Lexer lexer = setup("struct Point { int x; int y; };");
        assertEquals(new KeywordToken(TokenType.STRUCT, new Position(1, 1)), lexer.nextToken(), "Expected STRUCT");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 8), "Point"), lexer.nextToken(), "Expected point identifier");
        assertEquals(new KeywordToken(TokenType.CURLY_BRACKET_OPEN, new Position(1, 14)), lexer.nextToken(), "Expected CURLY_BRACKET_OPEN");
        assertEquals(new KeywordToken(TokenType.INT, new Position(1, 16)), lexer.nextToken(), "Expected INT");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 20), "x"), lexer.nextToken(), "Expected x identifier");
        assertEquals(new KeywordToken(TokenType.SEMICOLON, new Position(1, 21)), lexer.nextToken(), "Expected SEMICOLON");
        assertEquals(new KeywordToken(TokenType.INT, new Position(1, 23)), lexer.nextToken(), "Expected INT");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 27), "y"), lexer.nextToken(), "Expected y identifier");
        assertEquals(new KeywordToken(TokenType.SEMICOLON, new Position(1, 28)), lexer.nextToken(), "Expected SEMICOLON");
        assertEquals(new KeywordToken(TokenType.CURLY_BRACKET_CLOSE, new Position(1, 30)), lexer.nextToken(), "Expected CURLY_BRACKET_CLOSE");
        assertEquals(new KeywordToken(TokenType.SEMICOLON, new Position(1, 31)), lexer.nextToken(), "Expected SEMICOLON");
    }

    @Test
    void testSVariantTypeDeclaration() {
        Lexer lexer = setup("variant Point { int x; int y; };");
        assertEquals(new KeywordToken(TokenType.VARIANT, new Position(1, 1)), lexer.nextToken(), "Expected VARIANT");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 9), "Point"), lexer.nextToken(), "Expected point identifier");
        assertEquals(new KeywordToken(TokenType.CURLY_BRACKET_OPEN, new Position(1, 15)), lexer.nextToken(), "Expected CURLY_BRACKET_OPEN");
        assertEquals(new KeywordToken(TokenType.INT, new Position(1, 17)), lexer.nextToken(), "Expected INT");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 21), "x"), lexer.nextToken(), "Expected x identifier");
        assertEquals(new KeywordToken(TokenType.SEMICOLON, new Position(1, 22)), lexer.nextToken(), "Expected SEMICOLON");
        assertEquals(new KeywordToken(TokenType.INT, new Position(1, 24)), lexer.nextToken(), "Expected INT");
        assertEquals(new StringToken(TokenType.IDENTIFIER, new Position(1, 28), "y"), lexer.nextToken(), "Expected y identifier");
        assertEquals(new KeywordToken(TokenType.SEMICOLON, new Position(1, 29)), lexer.nextToken(), "Expected SEMICOLON");
        assertEquals(new KeywordToken(TokenType.CURLY_BRACKET_CLOSE, new Position(1, 31)), lexer.nextToken(), "Expected CURLY_BRACKET_CLOSE");
        assertEquals(new KeywordToken(TokenType.SEMICOLON, new Position(1, 32)), lexer.nextToken(), "Expected SEMICOLON");
    }
}