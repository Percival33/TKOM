package org.siu.ast.function;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Parameter;
import org.siu.ast.BlockStatement;
import org.siu.ast.Statement;
import org.siu.ast.type.TypeDeclaration;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.List;
import java.util.Optional;

@ToString(exclude = "block")
@EqualsAndHashCode(exclude = "position")
@Value
public class FunctionDefinitionStatement implements Statement {
    String name;
    List<Parameter> parameters;
    Optional<TypeDeclaration> returnType;
    BlockStatement block;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
