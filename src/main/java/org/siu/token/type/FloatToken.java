package org.siu.token.type;

import lombok.Value;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import static org.siu.token.TokenType.FLOAT_CONSTANT;

@Value
public class FloatToken implements Token{
    public TokenType getType() {
        return FLOAT_CONSTANT;
    }

    Position position;
    Float value;
}
