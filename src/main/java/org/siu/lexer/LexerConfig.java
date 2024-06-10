package org.siu.lexer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LexerConfig {
    public static final int MAX_FRACTIONAL_DIGITS = 7;
    public static final String LINE_BREAK = "\n";
    public static final int MAX_IDENTIFIER_LENGTH = 100;
    public static final String ESCAPE_SYMBOL = "\\";
    public static final String STRING_ENCLOSING_CHARACTER = "\"";
    public static final Map<String, Character> charactersToEscape = Map.of(
            "t", '\t',   // Tab
            "'", '\'',   // Single quote
            "\"", '\"',  // Double quote
            "\\", '\\',  // Backslash
            "n", '\n'    // New line
    );
}
