package org.siu.lexer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LexerConfig {
    public static final int MAX_FRACTIONAL_DIGITS = 10;
    public static final String LINE_BREAK = "\n";
    public static final int MAX_IDENTIFIER_LENGTH = 100;
    public static final String ESCAPE_SYMBOL = "\\";
    public static final String STRING_ENCLOSING_CHARACTER = "\"";
}
