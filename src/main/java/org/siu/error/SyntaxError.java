package org.siu.error;

import lombok.Getter;
import org.siu.token.Position;
@Getter
public class SyntaxError extends ParserError{
    public SyntaxError(Position position) {
        super(position);
    }
}
