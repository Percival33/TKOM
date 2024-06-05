package org.siu.error;

import org.siu.token.Position;

public class MissingTypeAfterConstException extends ParserError{
    public MissingTypeAfterConstException(Position position) {
        super(position);
    }
}
