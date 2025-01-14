package org.siu.interpreter;

import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.LogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.relation.*;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.statement.*;
import org.siu.ast.type.BooleanExpression;
import org.siu.ast.type.FloatExpression;
import org.siu.ast.type.IntegerExpression;
import org.siu.ast.type.StringExpression;
import org.siu.interpreter.builtin.PrintFunction;

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

    void visit(final ReturnStatement statement);

    void visit(final DeclarationStatement statement);

    void visit(final BlockStatement blockStatement);

    void visit(final AssignmentStatement assignmentStatement);

    void visit(final VariantTypeDefinitionStatement variantTypeDefinitionStatement);

    void visit(final StructTypeDefinitionStatement structTypeDefinitionStatement);

    void visit(final ConstStatement statement);

    void visit(final FunctionDefinitionStatement statement);

    void visit(final StructDeclarationExpression statement);

    void visit(final StructMemberAssignmentStatement statement);

    void visit(final VariantAssignmentStatement statement);

    //    Match statement
    void visit(final MatchStatement matchStatement);

    void visit(final MatchCaseStatement matchCaseStatement);

    //    Simple type expression
    void visit(final IntegerExpression expression);

    void visit(final FloatExpression expression);

    void visit(final StringExpression expression);

    void visit(final BooleanExpression expression);

    //    Complex type expression
    void visit(final VariantDeclarationExpression expression);

    void visit(final StructMemberExpression expression);


    void visit(final IdentifierExpression identifierExpression);

    void visit(final FunctionCallExpression expression);


    //    RelationExpression
    void visit(final RelationExpression expression);

    void visit(final EqualityRelationalExpression expression);

    //    ArithmeticExpression
    void visit(final BinaryArithmeticExpression expression);

    void visit(final NegateArithmeticExpression negateArithmeticExpression);

    //    LogicalExpression
    void visit(final LogicalExpression expression);

    void visit(final NegateLogicalExpression expression);


    //    Factor
    void visit(final CastedFactorExpression castedFactorExpression);

    void visit(final CopiedValueExpression copiedFactorExpression);

    //    Custom
    void visit(final PrintFunction expression);
}

