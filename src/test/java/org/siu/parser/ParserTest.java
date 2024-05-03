package org.siu.parser;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private final ErrorHandler errorHandler = Mockito.mock(ErrorHandler.class);

    Lexer setup(String text) {
        return new LexerImpl(text, errorHandler);
    }

    @Test
    public void addOperatorTest() throws Exception {
        String s = "a+2";
        Lexer lexer = setup(s);
        Parser parser = new Parser(lexer);

    }
}