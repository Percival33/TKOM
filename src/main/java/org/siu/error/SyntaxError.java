package org.siu.error;

import lombok.Getter;
import org.siu.token.Position;
@Getter
public class SyntaxError extends ParserError {
    private String details;

    public SyntaxError(Position position, String details) {
        super(position);
        this.details = details;
    }

    public SyntaxError(Position position) {
        super(position);
    }
}
