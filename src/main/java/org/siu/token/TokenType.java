package org.siu.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@NoArgsConstructor
public enum TokenType {
    END_OF_FILE("\u0003"),
    INTEGER_CONSTANT,
    INT("int"),
    STRING_CONSTANT,
    STRING("string"),

    BOOLEAN_CONSTANT,
    BOOL("bool"),

    BOOLEAN_TRUE("true"),
    BOOLEAN_FALSE("false"),
    ;

    private TokenType tokenType;
    private String keyword;

    TokenType(String keyword) {
        this.keyword = keyword;
    }

    TokenType(TokenType type, String keyword) {
        this.tokenType = type;
        this.keyword = keyword;
    }

    public static Boolean matchBoolean(String name) {
        return Arrays.stream(values()).filter(tokenType -> tokenType == BOOLEAN_FALSE || tokenType == BOOLEAN_TRUE).anyMatch(tokenType -> tokenType.getKeyword().equals(name));
    }
}
