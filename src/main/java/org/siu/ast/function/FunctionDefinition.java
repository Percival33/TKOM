package org.siu.ast.function;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Argument;
import org.siu.ast.BlockStatement;
import org.siu.ast.type.ValueType;
import org.siu.token.Position;

import java.util.List;
import java.util.Optional;

@ToString(exclude = "block")
@EqualsAndHashCode(exclude = "position")
@Value
public class FunctionDefinition {
    String name;
    List<Argument> parameters;
    Optional<ValueType> returnType;
    BlockStatement block;
    Position position;
}
