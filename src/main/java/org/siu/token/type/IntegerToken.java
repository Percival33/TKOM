package org.siu.token.type;

import lombok.Value;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

@Value
public class IntegerToken implements Token {
    Position position;
    Integer value;

    public TokenType getType() {
        return TokenType.INTEGER;
    }
}
