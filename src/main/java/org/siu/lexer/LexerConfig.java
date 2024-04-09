package org.siu.lexer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LexerConfig {
    public static final int MAX_FRACTIONAL_DIGITS = 10;
    public static int MAX_IDENTIFIER_LENGTH = 100;
    public static String ESCAPE_SYMBOL = "\\";
    public static String END_OF_LINE = "\n";
    public static String STRING_ENCLOSING_CHARACTER = "\"";
}
