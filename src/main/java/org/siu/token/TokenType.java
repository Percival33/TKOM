package org.siu.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@NoArgsConstructor
public enum TokenType {
    END_OF_FILE(""),

    VARIANT("variant"),
    STRUCT("struct"),
    CONST("const"),
    WHILE("while"),
    RETURN("return"),
    FUNCTION("fn"),

    INTEGER_CONSTANT,
    FLOAT_CONSTANT,
    STRING_CONSTANT,
    BOOLEAN_CONSTANT,

    INT("int"),
    FLOAT("float"),
    STRING("string"),
    BOOL("bool"),

    BOOLEAN_TRUE("true"),
    BOOLEAN_FALSE("false"),

    GREATER_EQUAL(">="),
    GREATER(">"),
    LESS("<"),
    LESS_EQUAL("<="),

    COPY_OPERATOR("@"),
    BRACKET_OPEN("("),
    BRACKET_CLOSE(")"),
    SQUARE_BRACKET_OPEN("{"),
    SQUARE_BRACKET_CLOSE("}"),

    COMPARE_EQUAL("=="),
    COMPARE_NOT_EQUAL("!="),
    MATCH("match"),

    SEMICOLON(";"),
    COLON(":"),
    DOUBLE_COLON("::"),
    DOT("."),
    COMMA(","),

    IDENTIFIER,

    AND("and"),
    OR("or"),
    NOT("not"),

    EQUAL("="),
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),

    IF("if"),
    ELSE("else"),
    ELIF("elif"),
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
}
