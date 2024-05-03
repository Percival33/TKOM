package org.siu.error;

import org.siu.token.Position;

public interface ErrorHandler {
    void handleLexerError(Exception e, Position p);
}
