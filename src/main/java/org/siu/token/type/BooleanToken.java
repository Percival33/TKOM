package org.siu.token.type;

import lombok.Value;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import static org.siu.token.TokenType.BOOLEAN_CONSTANT;

@Value
public class BooleanToken implements Token {
    Position position;
    Boolean value;
    @Override
    public TokenType getType() {
        return BOOLEAN_CONSTANT;
    }
}
