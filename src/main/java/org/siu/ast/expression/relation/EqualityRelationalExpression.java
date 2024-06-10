package org.siu.ast.expression.relation;

public interface EqualityRelationalExpression extends RelationExpression {
    boolean evaluate(String left, String right);
}
