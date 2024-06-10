package org.siu.ast.expression.arithmetic;

import org.siu.ast.expression.ArithmeticExpression;
import org.siu.ast.expression.Expression;
import org.siu.interpreter.Visitor;
import org.siu.interpreter.error.OperationNotSupported;

public interface BinaryArithmeticExpression extends ArithmeticExpression {
    Expression getLeft();

    Expression getRight();

    default void accept(Visitor visitor) {
        visitor.visit(this);
    }

    int evaluate(int first, int second);

    float evaluate(float first, float second);

    default String evaluate(String first, String second) {
        String className = this.getClass().getName();
        throw new OperationNotSupported(
                className.substring(className.lastIndexOf('.') + 1),
                "string",
                this.getPosition()
        );
    }
}
