package org.siu.token.type;

import lombok.Value;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

@Value
public class KeywordToken implements Token {
    TokenType type;
    Position position;

    @Override
    public <T> T getValue() {
        return null;
    }
}
