package org.siu.error;

import lombok.extern.slf4j.Slf4j;
import org.siu.token.Position;

import java.text.MessageFormat;

@Slf4j
public class MissingSemicolonError extends ParserError {
    public MissingSemicolonError(Position position) {
        super(position);
    }

    @Override
    public String toString() {
        return "Missing semicolon at the end of the statement";
    }
}
