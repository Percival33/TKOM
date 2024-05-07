package org.siu.token;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;


import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TokenUtils {
    public static final String END_OF_FILE = "";
    public static final String DOT = ".";

    public static final Map<String, TokenType> KEYWORDS = EnumSet.allOf(TokenType.class)
            .stream()
            .filter(token -> StringUtils.isNotBlank(token.getKeyword()))
            .filter(token -> StringUtils.isAlphanumeric(token.getKeyword()))
            .collect(Collectors.toUnmodifiableMap(TokenType::getKeyword, Function.identity()));

    public static final Map<String, TokenType> TWO_LETTER_SYMBOLS = Arrays.stream(
            new TokenType[]{
                    TokenType.LESS_EQUAL,
                    TokenType.GREATER_EQUAL,
                    TokenType.EQUAL,
                    TokenType.COMPARE_NOT_EQUAL,
                    TokenType.DOUBLE_COLON,
                    TokenType.MULTI_LINE_COMMENT_OPEN,
                    TokenType.MULTI_LINE_COMMENT_CLOSE,
            }
    ).collect(Collectors.toUnmodifiableMap(TokenType::getKeyword, Function.identity()));

    public static final Map<String, TokenType> BOOLEANS = Arrays.stream(
            new TokenType[]{
                    TokenType.BOOLEAN_FALSE,
                    TokenType.BOOLEAN_TRUE
            }
    ).collect(Collectors.toUnmodifiableMap(TokenType::getKeyword, Function.identity()));

    public static final Map<String, TokenType> SYMBOLS = Arrays.stream(
            new TokenType[]{
                    TokenType.BRACKET_OPEN,
                    TokenType.BRACKET_CLOSE,
                    TokenType.SQUARE_BRACKET_OPEN,
                    TokenType.SQUARE_BRACKET_CLOSE,
                    TokenType.SEMICOLON,
                    TokenType.COLON,
                    TokenType.COMMA,
                    TokenType.COPY_OPERATOR,
                    TokenType.DOT,
                    TokenType.PLUS,
                    TokenType.MINUS,
                    TokenType.MULTIPLY,
                    TokenType.DIVIDE,
                    TokenType.MODULO,
                    TokenType.ASSIGN,
                    TokenType.LESS,
                    TokenType.GREATER,
                    TokenType.SINGLE_LINE_COMMENT,
            }
    ).collect(Collectors.toUnmodifiableMap(TokenType::getKeyword, Function.identity()));

    public static boolean isSymbol(String s) {
        return StringUtils.isNotBlank(s) && StringUtils.isAsciiPrintable(s) && !StringUtils.isAlphanumeric(s);
    }

}