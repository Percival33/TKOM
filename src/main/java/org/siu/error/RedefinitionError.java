package org.siu.error;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.siu.token.Position;

@ToString
@Getter
public class RedefinitionError extends ParserError {

    public RedefinitionError(Position position) {
        super(position);
    }
}
