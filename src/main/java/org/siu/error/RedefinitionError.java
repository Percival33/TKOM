package org.siu.error;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.siu.token.Position;

@ToString
@Getter
public class RedefinitionError extends ParserError {
    private final String details;
    public RedefinitionError(String details, Position position) {
        super(position);
        this.details = details;
    }

}
