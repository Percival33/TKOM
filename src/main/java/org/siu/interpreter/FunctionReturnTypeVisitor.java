package org.siu.interpreter;

import io.vavr.Tuple3;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.siu.ast.BlockStatement;
import org.siu.ast.Node;
import org.siu.ast.Program;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.BinaryArithmeticExpression;
import org.siu.ast.expression.arithmetic.NegateArithmeticExpression;
import org.siu.ast.expression.logical.LogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.relation.EqualityRelationalExpression;
import org.siu.ast.expression.relation.RelationExpression;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.statement.*;
import org.siu.ast.type.*;
import org.siu.interpreter.builtin.PrintFunction;
import org.siu.interpreter.error.FunctionNotDefinedException;
import org.siu.interpreter.error.FunctionStackLimitException;
import org.siu.interpreter.error.InvalidReturnTypeException;
import org.siu.interpreter.error.UnsupportedOperationException;
import org.siu.token.Position;

import java.io.PrintStream;
import java.util.*;

import static org.siu.interpreter.InterpreterConfig.MAIN_FUNCTION_NAME;
import static org.siu.interpreter.InterpreterUtilities.BUILTIN_FUNCTIONS;

@Slf4j
@RequiredArgsConstructor
public class FunctionReturnTypeVisitor implements Visitor, Interpreter {
    private final Program program;
    private final PrintStream out;
    private final Map<String, FunctionDefinitionStatement> functionDefinitions = new HashMap<>(BUILTIN_FUNCTIONS);
    /*
     * Function call stack to keep track of the current function
     */
    private final Deque<Tuple3<String, Position, Optional<TypeDeclaration>>> functionCallStack = new ArrayDeque<>();
    private Position currentPosition = new Position(1, 1);
    private boolean errorOccurred = false;

    @Getter
    private String errorDetails = "";

    public boolean hasErrorOccurred() {
        return errorOccurred;
    }

    @Override
    public void execute() {
        try {
            callAccept(program);
        } catch (Exception e) {
            log.info("Error while function return type checking", e);
            out.println("Error while return type checking: " + e.getMessage());
            this.errorOccurred = true;
            this.errorDetails = e.getMessage();
        }
    }

    @Override
    public void visit(Program program) {
        functionDefinitions.putAll(program.getFunctionDefinitions());

        var mainFn = new FunctionCallExpression(MAIN_FUNCTION_NAME, List.of(), currentPosition);
        functionCallStack.add(new Tuple3<>(MAIN_FUNCTION_NAME, currentPosition, Optional.empty()));
        callAccept(mainFn);
    }

    @Override
    public void visit(WhileStatement statement) {
        callAccept(statement.getBlock());
    }

    @Override
    public void visit(IfStatement statement) {
        for (var block : statement.getIfInstructions()) {
            callAccept(block);
        }
        if (statement.getElseInstructions().isPresent()) {
            callAccept(statement.getElseInstructions().get());
        }
    }

    @Override
    public void visit(ReturnStatement statement) {
        if (functionCallStack.isEmpty()) {
            throw new RuntimeException("Return statement outside of function");
        }

        /*
         * _1 - function name
         * _2 - function position
         * _3 - function return type
         */
        var function = functionCallStack.getLast();
        if (function._3.isEmpty()) { // void function
            throw new InvalidReturnTypeException(function._1, function._2);
        }

        return;
    }

    @Override
    public void visit(DeclarationStatement statement) {
        callAccept(statement.getExpression());
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var statement : blockStatement.getStatements()) {
            callAccept(statement);
        }
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement) {
        callAccept(assignmentStatement.getValue());
    }

    @Override
    public void visit(VariantTypeDefinitionStatement variantTypeDefinitionStatement) {
        return;
    }

    @Override
    public void visit(StructTypeDefinitionStatement structTypeDefinitionStatement) {
        return;
    }

    @Override
    public void visit(ConstStatement statement) {
        return;
    }

    @Override
    public void visit(FunctionDefinitionStatement statement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(StructDeclarationExpression statement) {
        return;
    }

    @Override
    public void visit(StructMemberAssignmentStatement statement) {
        return;
    }

    @Override
    public void visit(VariantAssignmentStatement statement) {
        return;
    }

    @Override
    public void visit(MatchStatement matchStatement) {
        for (var matchCase : matchStatement.getStatements()) {
            callAccept(matchCase);
        }
    }

    @Override
    public void visit(MatchCaseStatement matchCaseStatement) {
        return;
    }

    @Override
    public void visit(IntegerExpression expression) {
        return;
    }

    @Override
    public void visit(FloatExpression expression) {
        return;
    }

    @Override
    public void visit(StringExpression expression) {
        return;
    }

    @Override
    public void visit(BooleanExpression expression) {
        return;
    }

    @Override
    public void visit(VariantDeclarationExpression expression) {
        return;
    }

    @Override
    public void visit(StructMemberExpression expression) {
        return;
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        return;
    }

    @Override
    public void visit(FunctionCallExpression expression) {
        if (!functionDefinitions.containsKey(expression.getIdentifier())) {
            throw new FunctionNotDefinedException(expression.getIdentifier());
        }
        var functionDeclaration = functionDefinitions.get(expression.getIdentifier());

        functionCallStack.add(new Tuple3<>(expression.getIdentifier(), functionDeclaration.getPosition(), functionDeclaration.getReturnType()));

        if(functionCallStack.size() > InterpreterConfig.MAX_STACK_SIZE) {
            throw new FunctionStackLimitException(expression.getIdentifier(), expression.getPosition());
        }

        callAccept(functionDeclaration.getBlock());
        functionCallStack.removeLast();
    }

    @Override
    public void visit(RelationExpression expression) {
        return;
    }

    @Override
    public void visit(EqualityRelationalExpression expression) {
        return;
    }

    @Override
    public void visit(BinaryArithmeticExpression expression) {
        return;
    }

    @Override
    public void visit(NegateArithmeticExpression negateArithmeticExpression) {
        return;
    }

    @Override
    public void visit(LogicalExpression expression) {
        return;
    }

    @Override
    public void visit(NegateLogicalExpression expression) {
        return;
    }

    @Override
    public void visit(CastedFactorExpression castedFactorExpression) {
        return;
    }

    @Override
    public void visit(CopiedValueExpression copiedFactorExpression) {
        return;
    }

    @Override
    public void visit(PrintFunction expression) {
        return;
    }

    private <T extends Node> void callAccept(T expression) {
        currentPosition = expression.getPosition();
        expression.accept(this);
    }
}
