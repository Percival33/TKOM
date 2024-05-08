package org.siu.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

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
    CURLY_BRACKET_OPEN("{"),
    CURLY_BRACKET_CLOSE("}"),

    EQUAL("=="),
    NOT_EQUAL("!="),
    MATCH("match"),

    SEMICOLON(";"),
    COLON(":"),
    DOUBLE_COLON("::"),
    DOT("."),
    COMMA(","),

    COMMENT,
    SINGLE_LINE_COMMENT("#"),
    MULTI_LINE_COMMENT_OPEN("/*"),
    MULTI_LINE_COMMENT_CLOSE("*/"),

    IDENTIFIER,

    AND("and"),
    OR("or"),
    NOT("not"),

    ASSIGN("="),
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
