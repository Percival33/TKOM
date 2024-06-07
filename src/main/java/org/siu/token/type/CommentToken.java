package org.siu.token.type;

import lombok.ToString;
import lombok.Value;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

@ToString
@Value
public class CommentToken implements Token {
    public TokenType getType() {
        return TokenType.COMMENT;
    }

    Position position;
    String value;
}
