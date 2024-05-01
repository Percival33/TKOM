package org.siu.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    void singleLineComment() {
        String val = "1";
        assertEquals("1", val);
    }
}