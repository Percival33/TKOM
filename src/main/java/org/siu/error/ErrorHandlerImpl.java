package org.siu.error;

import lombok.extern.slf4j.Slf4j;
import org.siu.token.Position;

@Slf4j
public class ErrorHandlerImpl implements ErrorHandler{
    @Override
    public void handleLexerError(Exception e, Position p) {
        System.out.println(e.toString() + "at" + p);
    }
}
