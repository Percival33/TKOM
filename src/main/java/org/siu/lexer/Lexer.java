package org.siu.lexer;

import org.siu.token.Token;

public interface Lexer {
    Token nextToken();
}
