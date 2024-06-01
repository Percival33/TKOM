package org.siu.ast.expression.relation;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.siu.ast.expression.Expression;
import org.siu.ast.expression.RelationExpression;
import org.siu.parser.Visitor;
import org.siu.token.Position;

/**
 * <
 */
@ToString(exclude = {"left", "right"})
@EqualsAndHashCode(exclude = "position")
@Value
public class LessExpression implements RelationExpression {
    Expression left;
    Expression right;
    Position position;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean evaluate(int left, int right) {
        return left < right;
    }
}
