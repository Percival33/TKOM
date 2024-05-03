package org.siu.token.type;

import lombok.Value;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import static org.siu.token.TokenType.STRING_CONSTANT;

@Value
public class StringToken implements Token {
    TokenType type;
    Position position;
    String value;
}
