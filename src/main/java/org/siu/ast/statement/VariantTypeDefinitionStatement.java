package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.siu.ast.Parameter;
import org.siu.ast.Statement;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

import java.util.List;

@EqualsAndHashCode(exclude = "position")
@Value
public class VariantTypeDefinitionStatement implements Statement {
    String name;
    List<Parameter> parameters;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
