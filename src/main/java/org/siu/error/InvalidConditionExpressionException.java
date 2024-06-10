package org.siu.error;

import org.siu.token.Position;

public class InvalidConditionExpressionException extends ParserError{
    public InvalidConditionExpressionException(Position position) {
        super(position);
    }
}
