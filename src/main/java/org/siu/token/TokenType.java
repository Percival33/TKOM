package org.siu.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@NoArgsConstructor
public enum TokenType {
    END_OF_FILE(""),
    INTEGER_CONSTANT,
    INT("int"),
    STRING_CONSTANT,
    STRING("string"),

    BOOLEAN_CONSTANT,
    BOOL("bool"),

    BOOLEAN_TRUE("true"),
    BOOLEAN_FALSE("false"),

    SEMICOLON(";"),
    COLON(":"),
    DOUBLE_COLON("::"),
    DOT("."),

    IDENTIFIER,

    EQUAL("="),
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%");

    private TokenType tokenType;
    private String keyword;

    TokenType(String keyword) {
        this.keyword = keyword;
    }

    TokenType(TokenType type, String keyword) {
        this.tokenType = type;
        this.keyword = keyword;
    }
}
