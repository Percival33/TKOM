package org.siu.error;

import lombok.Getter;
import org.siu.token.Position;
@Getter
public class ParserError extends RuntimeException {
    protected Position position;
    public ParserError(Position position) {
        super();
        this.position = position;
    }
}
