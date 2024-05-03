package org.siu.token.type;

import lombok.Value;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import static org.siu.token.TokenType.INTEGER_CONSTANT;

@Value
public class IntegerToken implements Token {
    public TokenType getType() {
        return INTEGER_CONSTANT;
    }

    Position position;
    Integer value;
}
