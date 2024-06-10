package org.siu.error;

import org.siu.token.Position;

public class EmptyVariantException extends ParserError {
    public EmptyVariantException(Position position) {
        super(position);
    }
}
