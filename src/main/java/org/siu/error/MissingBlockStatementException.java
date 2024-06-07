package org.siu.error;

import org.siu.token.Position;

public class MissingBlockStatementException extends ParserError{
    public MissingBlockStatementException(Position position) {
        super(position);
    }
}
