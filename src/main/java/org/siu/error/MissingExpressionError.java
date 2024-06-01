package org.siu.error;

import org.siu.token.Position;

import java.text.MessageFormat;

public class MissingExpressionError extends ParserError {
    public MissingExpressionError(Position position) {
        super(position);
    }

    @Override
    public String toString() {
        return String.format("Missing expression at the end of %s.", position.toString());
    }
}
