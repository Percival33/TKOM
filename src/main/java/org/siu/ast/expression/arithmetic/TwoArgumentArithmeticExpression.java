package org.siu.ast.expression.arithmetic;

import org.siu.ast.expression.ArithmeticExpression;
import org.siu.ast.expression.Expression;
import org.siu.parser.Visitor;

public interface TwoArgumentArithmeticExpression extends ArithmeticExpression {
    Expression getLeft();
    Expression getRight();

    default void accept(Visitor visitor) {
        visitor.visit(this);
    }

    int evaluate(int first, int second);
    float evaluate(float first, float second);
}
