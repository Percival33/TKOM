package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Parameter;
import org.siu.ast.Node;
import org.siu.ast.expression.Expression;
import org.siu.ast.Statement;
import org.siu.parser.Visitor;
import org.siu.token.Position;

import java.util.List;

@ToString(exclude = {"statement"})
@EqualsAndHashCode(exclude = "position")
@Value
public class ConstStatement implements Statement {
    String name;
    Statement statement;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
