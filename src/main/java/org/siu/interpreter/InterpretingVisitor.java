package org.siu.interpreter;

import io.vavr.Function3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.siu.ast.BlockStatement;
import org.siu.ast.Node;
import org.siu.ast.Program;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.AndLogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.logical.OrLogicalExpression;
import org.siu.ast.expression.relation.*;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.statement.*;
import org.siu.ast.type.*;
import org.siu.interpreter.error.*;
import org.siu.interpreter.error.UnsupportedOperationException;
import org.siu.interpreter.state.Context;
import org.siu.interpreter.state.Result;
import org.siu.interpreter.state.Value;
import org.siu.interpreter.state.Variable;
import org.siu.interpreter.state.value.BoolValue;
import org.siu.interpreter.state.value.FloatValue;
import org.siu.interpreter.state.value.IntValue;
import org.siu.interpreter.state.value.StringValue;
import org.siu.token.Position;

import java.io.PrintStream;
import java.util.*;

import static org.siu.interpreter.InterpreterConfig.MAIN_FUNCTION_NAME;
import static org.siu.interpreter.InterpreterConfig.MAX_STACK_SIZE;
import static org.siu.interpreter.InterpreterUtilities.*;

@Slf4j
@RequiredArgsConstructor
public class InterpretingVisitor implements Visitor, Interpreter {
    private final Program program;
    private final PrintStream out;
    private final Map<String, FunctionDefinitionStatement> functionDefinitions = new HashMap<>();
    private Result result = Result.empty();
    private Position currentPosition = new Position(1, 1);
    private Deque<Context> contexts = new ArrayDeque<>(List.of(GLOBAL_CONTEXT));

    @Override
    public void execute() {
        try {
            callAccept(program);
        } catch (Exception e) {
            log.error("Error while interpreting", e);
        }
    }

    @Override
    public void visit(Program program) {
        functionDefinitions.putAll(program.getFunctionDefinitions());

        for (var declaration : program.getDeclarations().values()) {
            callAccept(declaration);
        }


        var mainFn = new FunctionCallExpression(MAIN_FUNCTION_NAME, List.of(), currentPosition);
        callAccept(mainFn);
    }

    @Override
    public void visit(WhileStatement statement) {
        var condition = statement.getCondition();
        callAccept(condition);
        var value = retrieveResult(InterpreterUtilities.BOOL_TYPE);

        while (value.isBool()) {
            callAccept(statement.getBlock());

            if (result.isReturned()) {
                break;
            }

            callAccept(condition);
            value = retrieveResult(InterpreterUtilities.BOOL_TYPE);
        }
    }

    @Override
    public void visit(IfStatement statement) {
        var conditions = statement.getConditions();
        var blocks = statement.getIfInstructions();
        var elseBlock = statement.getElseInstructions();

        for (int i = 0; i < conditions.size(); i++) {
            callAccept(conditions.get(i));
            var value = retrieveResult(InterpreterUtilities.BOOL_TYPE);
            if (value.isBool()) {
                callAccept(blocks.get(i));
                return;
            }
        }
        elseBlock.ifPresent(this::callAccept);
    }

    @Override
    public void visit(ReturnStatement statement) {
        callAccept(statement.getExpression());
        result = result.toBuilder().returned(true).build();
    }

    @Override
    public void visit(DeclarationStatement statement) {
        var context = contexts.getLast();

        var type = statement.getParameter().getType();
        var name = statement.getParameter().getName();
        var expression = statement.getExpression();

        callAccept(expression);
        var value = retrieveResult(type);

        context.addVariable(new Variable(type, name, value));
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        var context = contexts.getLast();
        context.incrementScope();

        for (var statement : blockStatement.getStatements()) {
            callAccept(statement);
            if(result.isReturned()) {
                break;
            }
        }

        context.decrementScope();
    }

    @Override
    public void visit(AssignmentStatement statement) {
        var context = contexts.getLast();
        var previousValue = context.findVariable(statement.getName())
                .or(() -> GLOBAL_CONTEXT.findVariable(statement.getName()))
                .orElseThrow(NoVariableException::new);

        callAccept(statement.getValue());
        var value = retrieveResult(previousValue.getType());

        for (var currentContext : List.of(context, GLOBAL_CONTEXT)) {
            var updated = currentContext.updateVariable(statement.getName(), value);
            if (updated) {
                return;
            }
        }
    }

    @Override
    public void visit(VariantStatement variantStatement) {

    }

    @Override
    public void visit(StructStatement structStatement) {

    }

    @Override
    public void visit(ConstStatement constStatement) {

    }

    @Override
    public void visit(FunctionDefinitionStatement statement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(MatchStatement matchStatement) {

    }

    @Override
    public void visit(MatchCaseExpression matchCaseStatement) {

    }

    @Override
    public void visit(IntegerExpression expression) {
        result = Result.ok(new IntValue(expression.getValue()));
    }

    @Override
    public void visit(FloatExpression expression) {
        result = Result.ok(new FloatValue(expression.getValue()));
    }

    @Override
    public void visit(StringExpression expression) {
        result = Result.ok(new StringValue(expression.getValue()));
    }

    @Override
    public void visit(BooleanExpression expression) {
        result = Result.ok(new BoolValue(expression.getValue()));
    }

    @Override
    public void visit(VariantExpression expression) {

    }

    @Override
    public void visit(StructExpression expression) {

    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        var context = contexts.getLast();
        var variable = context.findVariable(identifierExpression.getIdentifier())
                .or(() -> GLOBAL_CONTEXT.findVariable(identifierExpression.getIdentifier()))
                .orElseThrow(NoVariableException::new);

        result = Result.ok(variable.getValue());
    }

    @Override
    public void visit(FunctionCallExpression expression) {
        if(!functionDefinitions.containsKey(expression.getIdentifier())) {
            throw new FunctionNotDefinedException(expression.getIdentifier());
        }

        var functionDeclaration = functionDefinitions.get(expression.getIdentifier());

        var arguments = expression.getArguments();

        if(arguments.size() != functionDeclaration.getParameters().size()) {
            throw new InvalidNumberOfArgumentsException(expression);
        }

        var context = new Context(functionDeclaration.getName(), expression.getPosition());

        for(int i = 0; i < arguments.size(); i++) {
            callAccept(arguments.get(i));
            var parameter = functionDeclaration.getParameters().get(i);
            var value = retrieveResult(parameter.getType());

            context.addVariable(new Variable(parameter.getType(),parameter.getName(), value));
        }

        contexts.addLast(context);
        if(contexts.size() > MAX_STACK_SIZE) {
            throw new FunctionStackLimitException();
        }

        callAccept(functionDeclaration.getBlock());

        // if return value
        if(functionDeclaration.getReturnType().isEmpty()) {
            result = Result.empty();
        } else if(result.isReturned()) {
            validateTypes(result.getValue().getType(), functionDeclaration.getReturnType().get());
            result = result.toBuilder().returned(false).build();
        } else {
            throw new FunctionDidNotReturnException();
        }

        contexts.removeLast();
    }

    private static final Map<TypeDeclaration, Function3<RelationExpression, Value, Value, Boolean>>
        RELATIONAL_OPERATIONS = Map.of(
            INT_TYPE, (expression, left, right) -> expression.evaluate(left.getInteger(), right.getInteger()),
            FLOAT_TYPE, (expression, left, right) -> expression.evaluate(left.getFloat(), right.getFloat())
        );

    @Override
    public void visit(RelationExpression expression) {
        callAccept(expression.getLeft());
        var left = retrieveResult();

        if (RELATIONAL_OPERATIONS.containsKey(left.getType())) {
            callAccept(expression.getRight());
            var right = retrieveResult(left.getType());
            var value = RELATIONAL_OPERATIONS.get(left.getType()).apply(expression, left, right);
            result = Result.ok(new BoolValue(value));
        } else {
            throw new CompareOperationNotSupportedForNonNumericTypes(expression.getPosition());
        }
    }

    private static final Map<TypeDeclaration, Function3<EqualityRelationalExpression, Value, Value, Boolean>>
            EQUALITY_OPERATORS = Map.of(
            INT_TYPE, (expression, left, right) -> expression.evaluate(left.getInteger(), right.getInteger()),
            FLOAT_TYPE, (expression, left, right) -> expression.evaluate(left.getFloat(), right.getFloat()),
            STRING_TYPE, (expression, left, right) -> expression.evaluate(left.getString(), right.getString())
    );

    @Override
    public void visit(EqualityRelationalExpression expression) {
        callAccept(expression.getLeft());
        var left = retrieveResult();

        if (EQUALITY_OPERATORS.containsKey(left.getType())) {
            callAccept(expression.getRight());
            var right = retrieveResult();
            var value = EQUALITY_OPERATORS.get(left.getType()).apply(expression, left, right);
            result = Result.ok(new BoolValue(value));
        } else {
            throw new CompareOperationNotSupportedForNonNumericTypes(expression.getPosition());
        }
    }

    private static final Map<TypeDeclaration, Function3<BinaryArithmeticExpression, Value, Value, Value>>
            ARITHMETIC_OPERATIONS = Map.of(
            INT_TYPE, (expression, left, right) -> new IntValue(expression.evaluate(left.getInteger(), right.getInteger())),
            FLOAT_TYPE, (expression, left, right) -> new FloatValue(expression.evaluate(left.getFloat(), right.getFloat()))
    );

    @Override
    public void visit(BinaryArithmeticExpression expression) {
        callAccept(expression.getLeft());
        var left = retrieveResult();

        if (ARITHMETIC_OPERATIONS.containsKey(left.getType())) {
            callAccept(expression.getRight());
            var right = retrieveResult(left.getType());
            var value = ARITHMETIC_OPERATIONS.get(left.getType()).apply(expression, left, right);
            result = Result.ok(value);
        } else {
            throw new ArithmeticOperationNotSupportedForNonNumericTypes(expression.getPosition());
        }
    }

    @Override
    public void visit(NegateArithmeticExpression negateArithmeticExpression) {

    }

    @Override
    public void visit(AndLogicalExpression andLogicalExpression) {

    }

    @Override
    public void visit(OrLogicalExpression orLogicalExpression) {

    }

    @Override
    public void visit(NegateLogicalExpression negateLogicalExpression) {

    }

    @Override
    public void visit(UnaryFactorExpression unaryFactorExpression) {

    }

    @Override
    public void visit(CastedFactorExpression castedFactorExpression) {

    }

    @Override
    public void visit(CopiedValueExpression copiedFactorExpression) {

    }

    private Value retrieveResult(TypeDeclaration type) {
        var value = retrieveResult();

        // TODO: struct / variant magic

        validateTypes(value.getType(), type);

        return value;
    }

    private Value retrieveResult() {
        if (!result.isPresent()) {
            throw new ExpressionDidNotEvaluateException();
        }

        return result.getValue();
    }

    private void validateTypes(TypeDeclaration provided, TypeDeclaration expected) {
        if (provided.getValueType() != expected.getValueType()) {
            throw new TypesDoNotMatchException(provided, expected);
        }

    }

    private <T extends Node> void callAccept(T expression) {
        currentPosition = expression.getPosition();
        expression.accept(this);
    }

    private void write(String message) {
        out.println(" ".repeat(0) + message);
    }
}