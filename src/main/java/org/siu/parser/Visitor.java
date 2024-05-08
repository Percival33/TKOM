package org.siu.parser;

import org.siu.ast.BlockStatement;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.AndLogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.logical.OrLogicalExpression;
import org.siu.ast.expression.relation.*;
import org.siu.ast.statement.*;
import org.siu.ast.type.BooleanExpression;
import org.siu.ast.type.FloatExpression;
import org.siu.ast.type.IntegerExpression;
import org.siu.ast.type.StringExpression;

/**
 * STATEMENT               = IF_STATEMENT
 * | WHILE_STATEMENT
 * | DECLARATION
 * | RETURN_STATEMENT
 * | ASSINGMENT
 * | MATCH
 * | FN_CALL;
 */
public interface Visitor {
//    Statement
    void visit(WhileStatement statement);

    void visit(IfStatement statement);

    void visit(ReturnStatement returnStatement);

    void visit(DeclarationStatement declarationStatement);

    void visit(BlockStatement blockStatement);

    void visit(AssignmentStatement assignmentStatement);

    void visit(VariantStatement variantStatement);

    void visit(StructStatement structStatement);

    void visit(ConstStatement constStatement);

    //    Match statement
    void visit(MatchStatement matchStatement);

    void visit(MatchCaseExpression matchCaseStatement);

    //    Simple type expression
    void visit(IntegerExpression integerExpression);

    void visit(FloatExpression expression);

    void visit(StringExpression stringExpression);

    void visit(BooleanExpression booleanExpression);

    //    Complex type expression
    void visit(VariantExpression variantExpression);

    void visit(StructExpression structExpression);


    void visit(IdentifierExpression identifierExpression);

    void visit(FunctionCallExpression functionCallExpression);


    //    RelationExpression
    void visit(LessExpression lessExpression);

    void visit(GreaterExpression greaterExpression);

    void visit(EqualExpression equalExpression);

    void visit(NotEqualExpression notEqualExpression);

    void visit(LessEqualExpression lessEqualExpression);

    void visit(GreaterEqualExpression greaterEqualExpression);

    //    ArithmeticExpression
    void visit(AddArithmeticExpression addArithmeticExpression);

    void visit(DivideArithmeticExpression divideArithmeticExpression);

    void visit(ModuloArithmeticExpression moduloArithmeticExpression);

    void visit(MultiplyArithmeticExpression multiplyArithmeticExpression);

    void visit(SubtractArithmeticExpression subtractArithmeticExpression);

    void visit(BinaryArithmeticExpression twoArgumentArithmeticExpression);

    void visit(NegateArithmeticExpression negateArithmeticExpression);

    //    LogicalExpression
    void visit(AndLogicalExpression andLogicalExpression);

    void visit(NegateLogicalExpression negateLogicalExpression);

    void visit(OrLogicalExpression orLogicalExpression);

    //    Factor
    void visit(UnaryFactorExpression unaryFactorExpression);

    void visit(CastedFactorExpression castedFactorExpression);

    void visit(CopiedValueExpression copiedFactorExpression);
}

