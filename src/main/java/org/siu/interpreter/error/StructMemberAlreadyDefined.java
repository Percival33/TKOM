package org.siu.interpreter.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.siu.token.Position;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StructMemberAlreadyDefined extends InterpreterException {
    private final String typeName;
    private final String memberName;

    public StructMemberAlreadyDefined(String typeName, String memberName, Position position) {
        super(position);
        this.typeName = typeName;
        this.memberName = memberName;
    }
}