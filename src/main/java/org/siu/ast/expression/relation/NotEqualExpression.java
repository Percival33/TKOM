package org.siu.ast.expression.relation;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.siu.ast.expression.Expression;
import org.siu.interpreter.Visitor;
import org.siu.token.Position;

/**
 * !=
 */
@ToString(exclude = {"left", "right"})
@EqualsAndHashCode(exclude = "position")
@Value
public class NotEqualExpression implements EqualityRelationalExpression {
    Expression left;
    Expression right;
    Position position;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean evaluate(int left, int right) {
        return left != right;
    }

    @Override
    public boolean evaluate(float left, float right) {
        return left != right;
    }

    @Override
    public boolean evaluate(String left, String right) {
        return !StringUtils.equals(left, right);
    }
}
