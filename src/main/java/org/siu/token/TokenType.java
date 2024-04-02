package org.siu.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum TokenType {
    END_OF_FILE("\u0003"),
    INTEGER,
    INT("int")
    ;

    private TokenType tokenType;
    private String keyword;

    TokenType(String keyword) { this.keyword = keyword; }
    TokenType(TokenType type, String keyword) { this.tokenType = type; this.keyword = keyword; }
}