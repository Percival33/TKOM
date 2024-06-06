package org.siu.ast.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.Statement;
import org.siu.ast.expression.Expression;
import org.siu.ast.expression.StructExpression;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

@ToString(exclude = {"value"})
@EqualsAndHashCode(exclude = "position")
@Value
public class StructMemberAssignmentStatement implements Statement {
    StructExpression struct;
    Expression value;
    Position position;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
