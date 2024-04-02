package org.siu.error;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorHandlerImpl implements ErrorHandler{
    @Override
    public void handleLexerError(Exception e) {
        System.out.println(e.toString());
    }
}
