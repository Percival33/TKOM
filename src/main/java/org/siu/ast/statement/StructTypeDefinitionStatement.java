package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Parameter;
import org.siu.ast.Statement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.List;

@ToString(exclude = {"parameters"})
@EqualsAndHashCode(exclude = "position")
@Value
public class StructTypeDefinitionStatement implements CustomTypeStatement {
    String name;
    List<Parameter> parameters;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
