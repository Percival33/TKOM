package org.siu.token;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TokenUtils {
    public static String END_OF_FILE = "";
    public static String DOT = ".";

    public static final Map<String, TokenType> KEYWORDS = EnumSet.allOf(TokenType.class)
            .stream()
            .filter(token -> StringUtils.isNotBlank(token.getKeyword()))
            .filter(token -> StringUtils.isAlphanumeric(token.getKeyword()))
            .collect(Collectors.toUnmodifiableMap(TokenType::getKeyword, Function.identity()));

    // TODO: explicite podać wartości tokenów 2 składnikowych
    public static Map<String, TokenType> OPERATORS = EnumSet.allOf(TokenType.class)
                .stream()
                .filter(token -> isSymbol(token.getKeyword()))
                .filter(token -> token.getKeyword().length() == 1)
            .collect(Collectors.toUnmodifiableMap(TokenType::getKeyword, Function.identity()));

    public static boolean isBoolean(TokenType token) {
        return token == TokenType.BOOLEAN_FALSE || token == TokenType.BOOLEAN_TRUE;
    }
    public static boolean isSymbol(String s) {
        return StringUtils.isNotBlank(s) && StringUtils.isAsciiPrintable(s) && !StringUtils.isAlphanumeric(s);
    }

}