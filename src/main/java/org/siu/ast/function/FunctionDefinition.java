package org.siu.ast.function;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Block;
import org.siu.token.Position;

import java.util.List;
import java.util.Optional;

@ToString(exclude = "block")
@EqualsAndHashCode(exclude = "position")
@Value
public class FunctionDefinition {
    String name;
    List<FunctionParameter> parameters;
    Optional<FunctionParameter> returnType;
    Block block;
    Position position;
}
