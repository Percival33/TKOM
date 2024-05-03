package org.siu.ast.expression;

public interface ArithmeticExpression extends Expression{
    Expression getLeft();
    Expression getRight();

    int evaluate(int left, int right);
}
