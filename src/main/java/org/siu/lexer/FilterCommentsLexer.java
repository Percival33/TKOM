package org.siu.lexer;

import lombok.extern.slf4j.Slf4j;
import org.siu.token.Token;
import org.siu.token.TokenType;

@Slf4j
public class FilterCommentsLexer implements Lexer {
    private final Lexer lexer;

    public FilterCommentsLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public Token nextToken() {
        Token token = lexer.nextToken();

        while (token.getType() == TokenType.COMMENT) {
            token = lexer.nextToken();
        }

        return token;
    }
}
