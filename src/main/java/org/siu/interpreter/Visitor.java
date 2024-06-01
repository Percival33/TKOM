package org.siu.interpreter;

import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.AndLogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.logical.OrLogicalExpression;
import org.siu.ast.expression.relation.*;
import org.siu.ast.function.FunctionDefinitionStatement;
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
    void visit(final Program program);

    //    Statement
    void visit(final WhileStatement statement);

    void visit(final IfStatement statement);

    void visit(final ReturnStatement returnStatement);

    void visit(final DeclarationStatement declarationStatement);

    void visit(final BlockStatement blockStatement);

    void visit(final AssignmentStatement assignmentStatement);

    void visit(final VariantStatement variantStatement);

    void visit(final StructStatement structStatement);

    void visit(final ConstStatement constStatement);

    void visit(final FunctionDefinitionStatement functionDefinitionStatement);

    //    Match statement
    void visit(final MatchStatement matchStatement);

    void visit(final MatchCaseExpression matchCaseStatement);

    //    Simple type expression
    void visit(final IntegerExpression integerExpression);

    void visit(final FloatExpression expression);

    void visit(final StringExpression stringExpression);

    void visit(final BooleanExpression booleanExpression);

    //    Complex type expression
    void visit(final VariantExpression variantExpression);

    void visit(final StructExpression structExpression);


    void visit(final IdentifierExpression identifierExpression);

    void visit(final FunctionCallExpression functionCallExpression);


    //    RelationExpression
    void visit(final LessExpression lessExpression);

    void visit(final GreaterExpression greaterExpression);

    void visit(final EqualExpression equalExpression);

    void visit(final NotEqualExpression notEqualExpression);

    void visit(final LessEqualExpression lessEqualExpression);

    void visit(final GreaterEqualExpression greaterEqualExpression);

    //    ArithmeticExpression
    void visit(final AddArithmeticExpression addArithmeticExpression);

    void visit(final DivideArithmeticExpression divideArithmeticExpression);

    void visit(final ModuloArithmeticExpression moduloArithmeticExpression);

    void visit(final MultiplyArithmeticExpression multiplyArithmeticExpression);

    void visit(final SubtractArithmeticExpression subtractArithmeticExpression);

    void visit(final BinaryArithmeticExpression twoArgumentArithmeticExpression);

    void visit(final NegateArithmeticExpression negateArithmeticExpression);

    //    LogicalExpression
    void visit(final AndLogicalExpression andLogicalExpression);

    void visit(final NegateLogicalExpression negateLogicalExpression);

    void visit(final OrLogicalExpression orLogicalExpression);

    //    Factor
    void visit(final UnaryFactorExpression unaryFactorExpression);

    void visit(final CastedFactorExpression castedFactorExpression);

    void visit(final CopiedValueExpression copiedFactorExpression);
}

