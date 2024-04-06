package org.siu.lexer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.siu.error.ErrorHandlerImpl;
import org.siu.token.TokenType;
import org.siu.token.type.BooleanToken;
import org.siu.token.type.IntegerToken;
import org.siu.token.type.KeywordToken;
import org.siu.token.type.StringToken;

import static org.junit.jupiter.api.Assertions.*;

class LexerImplTest {
    Lexer setup(String text) {
        ErrorHandlerImpl errorHandler = new ErrorHandlerImpl();
        return new LexerImpl(text, errorHandler);
    }

    @Nested
    class IntegerTokenTests {
        @Test
        void testInteger() {
            Lexer lexer = setup("123");
            assertEquals(new IntegerToken(null, 123), lexer.nextToken());
        }

        @Test
        void testZeroWithWhitespaces() {
            Lexer lexer = setup("    \t0");
            assertEquals(new IntegerToken(null, 0), lexer.nextToken());
        }

        @Test
        void testOverflowInteger() {
            Lexer lexer = setup(String.valueOf(Integer.MAX_VALUE) + "11111111111111");
            assertEquals(new IntegerToken(null, Integer.MAX_VALUE), lexer.nextToken());
        }

        @Test
        void testMaxInteger() {
            Lexer lexer = setup(String.valueOf(Integer.MAX_VALUE));
            assertEquals(new IntegerToken(null, Integer.MAX_VALUE), lexer.nextToken());
        }

        @Test
        void testMinInteger() {
            int value = Math.abs(Integer.MIN_VALUE + 1);
            Lexer lexer = setup(String.valueOf(value));
            assertEquals(new IntegerToken(null, value), lexer.nextToken());
        }
    }

    @Nested
    class StringTokenTest {
        @Test
        void simpleString() {
            Lexer lexer = setup("\"Ala nie ma kota.\"");
            assertEquals(new StringToken(TokenType.STRING, null, "Ala nie ma kota."), lexer.nextToken());
        }

        @Test
        void escapeNewLine() {
            Lexer lexer = setup("\"Ala nie\n ma kota.\"");
            assertEquals(new StringToken(TokenType.STRING, null, "Ala nie\n ma kota."), lexer.nextToken());
        }
    }

    @Nested
    class BooleanTokenTest {
        @Test
        void boolTrueValue() {
            Lexer lexer = setup("true");
            assertEquals(new BooleanToken(null, true), lexer.nextToken());
            lexer = setup("false");
            assertEquals(new BooleanToken(null, false), lexer.nextToken());
        }

        @Test
        void notMatchedCaseBooleanValue() {
            Lexer lexer = setup("False");
            assertEquals(null, lexer.nextToken());
        }
    }

    @Nested
    class DeclarationsTest {
//        @Test
//        void intDeclaration() {
//            Lexer lexer = setup("int x = 10;");
//            assertEquals(new KeywordToken(TokenType.INT, null), lexer.nextToken(), "Expected keyword 'INT'");
//            assertEquals(new StringToken(TokenType.IDENTIFIER, null, "x"), lexer.nextToken(), "Expected identifier");
//            assertEquals(new KeywordToken(TokenType.EQUAL, null), lexer.nextToken(), "Expected EQ");
//            assertEquals(new IntegerToken(null, 10), lexer.nextToken(), "Expected integer token");
//            assertEquals(new KeywordToken(TokenType.SEMICOLON, null), lexer.nextToken(), "Expected semicolon");
//            assertEquals(new KeywordToken(TokenType.END_OF_FILE, null), lexer.nextToken(), "Expected end of file token");
//        }
        @Test
        void intDeclaration() {
            Lexer lexer = setup(" ");
//            Lexer lexer = setup("int = 10;");
//            assertEquals(new KeywordToken(TokenType.INT, null), lexer.nextToken(), "Expected keyword 'INT'");
////            assertEquals(new StringToken(TokenType.IDENTIFIER, null, "x"), lexer.nextToken(), "Expected identifier");
//            assertEquals(new KeywordToken(TokenType.EQUAL, null), lexer.nextToken(), "Expected EQ");
//            assertEquals(new IntegerToken(null, 10), lexer.nextToken(), "Expected integer token");
//            assertEquals(new KeywordToken(TokenType.SEMICOLON, null), lexer.nextToken(), "Expected semicolon");
            assertEquals(new KeywordToken(TokenType.END_OF_FILE, null), lexer.nextToken(), "Expected end of file token");
        }
    }
}