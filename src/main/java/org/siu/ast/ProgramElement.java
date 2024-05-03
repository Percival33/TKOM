package org.siu.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.siu.token.Position;
@Getter
@Setter
@AllArgsConstructor
public abstract class ProgramElement {
    protected String name;
    protected Position position;
}
