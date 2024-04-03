package org.siu.token.type;

import lombok.Value;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import static org.siu.token.TokenType.STRING_CONSTANT;

@Value
public class StringToken implements Token {
    Position position;
    String value;
    @Override
    public TokenType getType() {
        return STRING_CONSTANT;
    }
}
