package org.siu.ast.expression;

import org.siu.parser.Visitor;

public interface ArithmeticExpression extends Expression{
    Expression getLeft();
    Expression getRight();

    int evaluate(int left, int right);
//    TODO: add parent interface
//    @Override
//    default void accept(Visitor visitor) {
//        visitor.visit(this);
//    }
}
