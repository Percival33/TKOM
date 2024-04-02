package org.siu.lexer;

import org.junit.jupiter.api.Test;
import org.siu.error.ErrorHandlerImpl;
import org.siu.token.type.IntegerToken;

import static org.junit.jupiter.api.Assertions.*;

class LexerImplTest {
    Lexer setup(String text) {
        ErrorHandlerImpl errorHandler = null;
        return new LexerImpl(text, errorHandler);
    }
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
        // TODO: fix text
        Lexer lexer = setup(String.valueOf(Integer.MAX_VALUE) + "1");
        assertNotEquals(new IntegerToken(null, -1), lexer.nextToken());
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