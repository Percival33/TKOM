package org.siu.ast.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.siu.token.TokenType;

import java.util.EnumSet;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum ValueType {
    INT(TokenType.INT),
    FLOAT(TokenType.FLOAT),
    BOOL(TokenType.BOOL),
    STRING(TokenType.STRING),
//    VARIANT(TokenType.VARIANT),
//    STRUCT(TokenType.STRUCT),
    CUSTOM(TokenType.IDENTIFIER),
    ;


    private final TokenType type;

    public static Optional<ValueType> of(TokenType provided) {
        return EnumSet.allOf(ValueType.class)
                .stream()
                .filter(it -> it.getType() == provided)
                .findFirst();
    }
}
