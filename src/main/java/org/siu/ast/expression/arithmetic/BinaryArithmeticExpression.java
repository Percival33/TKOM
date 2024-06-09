package org.siu.ast.expression.arithmetic;

import org.siu.ast.expression.ArithmeticExpression;
import org.siu.ast.expression.Expression;
import org.siu.interpreter.Visitor;

public interface BinaryArithmeticExpression extends ArithmeticExpression {
    Expression getLeft();
    Expression getRight();

    default void accept(Visitor visitor) {
        visitor.visit(this);
    }

    int evaluate(int first, int second);
    float evaluate(float first, float second);
    default String evaluate(String first, String second) {
        throw new UnsupportedOperationException(this.getClass().getName() + " is not supported for 'string' type at " + this.getPosition());
    }
}
