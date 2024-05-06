package org.siu.parser;

import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.AndLogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.logical.OrLogicalExpression;
import org.siu.ast.expression.relation.LessExpression;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.statement.IfStatement;
import org.siu.ast.statement.WhileStatement;
import org.siu.ast.type.BooleanExpression;
import org.siu.ast.type.FloatExpression;
import org.siu.ast.type.IntegerExpression;
import org.siu.ast.type.StringExpression;

/**
 * STATEMENT               = IF_STATEMENT
 *                         | WHILE_STATEMENT
 *                         | DECLARATION
 *                         | RETURN_STATEMENT
 *                         | ASSINGMENT
 *                         | MATCH
 *                         | FN_CALL;
 */
public interface Visitor {
    // TODO: implement rest of statements
    void visit(WhileStatement statement);
    void visit(IfStatement statement);

//    Simple type expression
    void visit(IntegerExpression integerExpression);
    void visit(FloatExpression expression);
    void visit(StringExpression stringExpression);
    void visit(BooleanExpression booleanExpression);

    void visit(IdentifierExpression identifierExpression);
    void visit(StructExpression structExpression);

    void visit(FunctionCallExpression functionCallExpression);



    void visit(DeclarationStatement declarationStatement);

    void visit(LessExpression lessExpression);


//    ArithmeticExpression
    void visit(AddArithmeticExpression addArithmeticExpression);
    void visit(DivideArithmeticExpression divideArithmeticExpression);
    void visit(ModuloArithmeticExpression moduloArithmeticExpression);
    void visit(MultiplyArithmeticExpression multiplyArithmeticExpression);
    void visit(SubtractArithmeticExpression subtractArithmeticExpression);
    void visit(TwoArgumentArithmeticExpression twoArgumentArithmeticExpression);
    void visit(NegateArithmeticExpression negateArithmeticExpression);

//    LogicalExpression
    void visit(AndLogicalExpression andLogicalExpression);
    void visit(NegateLogicalExpression negateLogicalExpression);
    void visit(OrLogicalExpression orLogicalExpression);

//    Factor
    void visit(UnaryFactorExpression unaryFactorExpression);
    void visit(CastedFactorExpression castedFactorExpression);
    void visit(CopiedFactorExpression copiedFactorExpression);

}
