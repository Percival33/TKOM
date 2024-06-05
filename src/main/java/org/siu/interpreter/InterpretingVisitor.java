package org.siu.interpreter;

import io.vavr.Function3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.siu.ast.BlockStatement;
import org.siu.ast.Node;
import org.siu.ast.Program;
import org.siu.ast.Statement;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.LogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.relation.*;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.statement.*;
import org.siu.ast.type.*;
import org.siu.interpreter.builtin.PrintFunction;
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
import java.util.function.Function;

import static org.siu.interpreter.InterpreterConfig.MAIN_FUNCTION_NAME;
import static org.siu.interpreter.InterpreterConfig.MAX_STACK_SIZE;
import static org.siu.interpreter.InterpreterUtilities.*;

@Slf4j
@RequiredArgsConstructor
public class InterpretingVisitor implements Visitor, Interpreter {
    private final Program program;
    private final PrintStream out;
    private final Map<String, FunctionDefinitionStatement> functionDefinitions = new HashMap<>(BUILTIN_FUNCTIONS);
    private final Map<String, Statement> typeDefinitions = new HashMap<>();

    private final Deque<Context> contexts = new ArrayDeque<>(List.of(GLOBAL_CONTEXT));
    private Result result = Result.empty();
    private Position currentPosition = new Position(1, 1);

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
        typeDefinitions.putAll(program.getTypeDefinitions());

        for (var declaration : program.getDeclarations().values()) {
            callAccept(declaration);
        }

        for (var typeDefinition : program.getTypeDefinitions().values()) {
            callAccept(typeDefinition);
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
            if (result.isReturned()) {
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

        if(previousValue.isConstant()) {
            throw new ReassignConstVariableException(previousValue.getIdentifier(), statement.getPosition());
        }

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
    public void visit(VariantTypeDefinitionStatement variantTypeDefinitionStatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(StructTypeDefinitionStatement structStatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ConstStatement constStatement) {
        var context = contexts.getLast();

        var parameter = constStatement.getParameter();
        var statement = constStatement.getStatement();

        callAccept(statement.getExpression());
        var value = retrieveResult(parameter.getType());

        context.addVariable(new Variable(parameter.getType(), parameter.getName(), value, true));
    }

    @Override
    public void visit(FunctionDefinitionStatement statement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(StructDeclarationExpression statement) {
//        var context = contexts.getLast();
//
//        var type = statement.getParameter().getType();
//        var name = statement.getParameter().getName();
//        var expression = statement.getExpression();
//
//        callAccept(expression);
//        var value = retrieveResult(type);
//
//        context.addVariable(new Variable(type, name, value));
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
        var context = contexts.getLast();
        var variable = context.findVariable(expression.getStructName())
                .or(() -> GLOBAL_CONTEXT.findVariable(expression.getStructName()))
                .orElseThrow(NoVariableException::new);


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
        if (!functionDefinitions.containsKey(expression.getIdentifier())) {
            throw new FunctionNotDefinedException(expression.getIdentifier());
        }

        var functionDeclaration = functionDefinitions.get(expression.getIdentifier());

        var arguments = expression.getArguments();

        if (arguments.size() != functionDeclaration.getParameters().size()) {
            throw new InvalidNumberOfArgumentsException(expression);
        }

        var context = new Context(functionDeclaration.getName(), expression.getPosition());

        for (int i = 0; i < arguments.size(); i++) {
            callAccept(arguments.get(i));
            var parameter = functionDeclaration.getParameters().get(i);
            var value = retrieveResult(parameter.getType());

            context.addVariable(new Variable(parameter.getType(), parameter.getName(), value));
        }

        contexts.addLast(context);
        if (contexts.size() > MAX_STACK_SIZE) {
            throw new FunctionStackLimitException();
        }

        callAccept(functionDeclaration.getBlock());

        // if return value
        if (functionDeclaration.getReturnType().isEmpty()) {
            result = Result.empty();
        } else if (result.isReturned()) {
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
            FLOAT_TYPE, (expression, left, right) -> expression.evaluate(left.getFloatVal(), right.getFloatVal())
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
            FLOAT_TYPE, (expression, left, right) -> expression.evaluate(left.getFloatVal(), right.getFloatVal()),
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
            FLOAT_TYPE, (expression, left, right) -> new FloatValue(expression.evaluate(left.getFloatVal(), right.getFloatVal()))
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
        callAccept(negateArithmeticExpression.getExpression());
        var value = retrieveResult();

        if (Objects.equals(value.getType(), INT_TYPE)) {
            result = Result.ok(new IntValue(-value.getInteger()));
        } else if (Objects.equals(value.getType(), FLOAT_TYPE)) {
            result = Result.ok(new FloatValue(-value.getFloatVal()));
        } else {
            throw new ArithmeticOperationNotSupportedForNonNumericTypes(negateArithmeticExpression.getPosition());
        }
    }

    @Override
    public void visit(LogicalExpression expression) {
        callAccept(expression.getLeft());
        var left = retrieveResult(BOOL_TYPE);

        callAccept(expression.getRight());
        var right = retrieveResult(BOOL_TYPE);
        result = Result.ok(new BoolValue(expression.evaluate(left.isBool(), right.isBool())));
    }

    @Override
    public void visit(NegateLogicalExpression expression) {
        callAccept(expression.getExpression());
        var value = retrieveResult(BOOL_TYPE);
        result = Result.ok(new BoolValue(!value.isBool()));
    }

    @Override
    public void visit(UnaryFactorExpression unaryFactorExpression) {

    }

    private static final Map<TypeDeclaration, Map<TypeDeclaration, Function<Value, Value>>> CAST_OPERATIONS = Map.of(
            INT_TYPE, Map.of(
                    BOOL_TYPE, value -> new IntValue(value.isBool() ? 1 : 0),
                    FLOAT_TYPE, value -> new IntValue((int) value.getFloatVal()),
                    INT_TYPE, Function.identity(),
                    STRING_TYPE, value -> new IntValue(StringUtils.equals(value.getString(), "") ? 0 : 1)
            ),
            FLOAT_TYPE, Map.of(
                    BOOL_TYPE, value -> new FloatValue(value.isBool() ? 1.0F : 0.0F),
                    FLOAT_TYPE, Function.identity(),
                    INT_TYPE, value -> new FloatValue(value.getInteger()),
                    STRING_TYPE, value -> new FloatValue(StringUtils.equals(value.getString(), "") ? 0.0F : 1.0F)
            ),
            STRING_TYPE, Map.of(
                    BOOL_TYPE, value -> new StringValue(String.valueOf(value.isBool())),
                    INT_TYPE, value -> new StringValue(String.valueOf(value.getInteger())),
                    FLOAT_TYPE, value -> new StringValue(String.valueOf(value.getFloatVal())),
                    STRING_TYPE, Function.identity()
            )
    );

    @Override
    public void visit(CastedFactorExpression castedFactorExpression) {
        var type = castedFactorExpression.getType();

        if (!CAST_OPERATIONS.containsKey(type)) {
            throw new UnsupportedCastException();
        }

        var castHelper = CAST_OPERATIONS.get(type);
        var supportedTypes = castHelper.keySet();

        callAccept(castedFactorExpression.getExpression());
        var toCast = retrieveResult();

        if (!supportedTypes.contains(toCast.getType())) {
            throw new UnsupportedCastException();
        }
        var value = castHelper.get(toCast.getType()).apply(toCast);
        result = Result.ok(value);
    }

    @Override
    public void visit(CopiedValueExpression copiedFactorExpression) {

    }

    @Override
    public void visit(PrintFunction expression) {
        var context = contexts.getLast();

        var message = context.findVariable(PRINT_ARGUMENT)
            .map(Variable::getValue)
            .orElseThrow(NoVariableException::new);

        out.println(message.getString());
    }

    private Value retrieveResult(TypeDeclaration type) {
        var value = retrieveResult();

        if(type.getValueType() == ValueType.CUSTOM) {
            throw new RuntimeException("Custom type not supported.");
        }

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
