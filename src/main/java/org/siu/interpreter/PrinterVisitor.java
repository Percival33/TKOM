package org.siu.interpreter;

import lombok.RequiredArgsConstructor;
import org.siu.ast.BlockStatement;
import org.siu.ast.Node;
import org.siu.ast.Parameter;
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
import org.siu.token.Position;

import java.io.PrintStream;
import java.util.*;

@RequiredArgsConstructor
public class PrinterVisitor implements Visitor, Interpreter {
    //    private final ErrorHandler errorHandler;
    private final PrintStream out;
    private final Program program;

//    private final Deque<Context> contexts = new ArrayDeque<>(List.of(GLOBAL_CONTEXT));
//    private final Map<String, FunctionDefinitionStatement> functionDefinitions = new HashMap<>(BUILTIN_FUNCTIONS);
    private Position currentPosition = new Position(1,1 );
    private int indent = 0;

    @Override
    public void execute() {
        callAccept(program);
    }

    @Override
    public void visit(Program program) {
        program.getFunctionDefinitions().values().forEach(this::callAccept);
        program.getDeclarations().values().forEach(this::callAccept);
    }

    @Override
    public void visit(WhileStatement statement) {
        write("While Statement");
        indent++;
        write("Condition");
        callAccept(statement.getCondition());
        callAccept(statement.getBlock());
        indent--;
    }

    @Override
    public void visit(IfStatement statement) {
        write("If Statement");
        var conditions = statement.getConditions();
        var blocks = statement.getIfInstructions();
        var elseBlock = statement.getElseInstructions();
        indent++;
        for (int i = 0; i < conditions.size(); i++) {
            write("Condition");
            callAccept(conditions.get(i));
            callAccept(blocks.get(i));
            indent--;
        }
        if (elseBlock.isPresent()) {
            indent++;
            write("Else Block");
            callAccept(elseBlock.get());
            indent--;
        }
        indent--;
    }

    @Override
    public void visit(ReturnStatement statement) {
        write("Return Statement");
        indent++;
        callAccept(statement.getExpression());
        indent--;
    }

    @Override
    public void visit(DeclarationStatement statement) {
        write("Declaration Statement" + statement.getParameter().getName() + " = " + statement.getExpression());
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        write("BlockStatement");
        indent++;
        blockStatement.getStatementList().forEach(this::callAccept);
        indent--;
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement) {
        write("AssignmentStatement: " + assignmentStatement.getName());
        indent++;
        callAccept(assignmentStatement.getValue());
        indent--;
    }

    @Override
    public void visit(VariantStatement variantStatement) {
        write("VariantStatement" + variantStatement.getName());
        indent++;
        var params = variantStatement.getParameters();
        handleParams(params);
        indent--;
    }

    private void handleParams(List<Parameter> params) {
        write("Parameters");
        indent++;
        for(var param : params) {
            write(param.getName() + " -> " + param.getType());
        }
        indent--;
    }

    @Override
    public void visit(StructStatement structStatement) {
        write("StructStatement" + structStatement.getName());
        indent++;
        handleParams(structStatement.getParameters());
        indent--;
    }

    @Override
    public void visit(ConstStatement constStatement) {
        write("ConstStatement" + constStatement.getName());
    }

    @Override
    public void visit(FunctionDefinitionStatement functionDefinitionStatement) {
        var returnType = functionDefinitionStatement.getReturnType().isPresent() ? functionDefinitionStatement.getReturnType().get().toString() : "";
        var parameters = functionDefinitionStatement.getParameters();
        write("FunctionDefinitionStatement");
        indent++;
        write(functionDefinitionStatement.getName() + (returnType.equals("") ? "" : " -> " + returnType));
        handleParams(parameters);
        callAccept(functionDefinitionStatement.getBlock());
        indent--;
    }

    @Override
    public void visit(MatchStatement matchStatement) {
        write("MatchStatement" + matchStatement.getName());
        indent++;
        matchStatement.getStatements().forEach(this::callAccept);
        indent--;
    }

    @Override
    public void visit(MatchCaseExpression matchCaseStatement) {
        write("MatchCaseExpression" + matchCaseStatement.getVariantType() + "::" + matchCaseStatement.getMember());
        indent++;
        write("Variable" + matchCaseStatement.getVariable() + " -> " + matchCaseStatement.getExpression());
        indent--;
    }

    @Override
    public void visit(IntegerExpression expression) {
        write("IntegerExpression: " + expression.getValue());
    }

    @Override
    public void visit(FloatExpression expression) {
        write("FloatExpression" + expression.getValue());
    }

    @Override
    public void visit(StringExpression expression) {
        write("StringExpression" + expression.getValue());
    }

    @Override
    public void visit(BooleanExpression expression) {
        write("BooleanExpression" + expression.getValue());
    }

    @Override
    public void visit(VariantExpression expression) {
        write("VariantExpression" + expression.getFieldName());
        indent++;
        callAccept(expression.getExpression());
        indent--;
    }

    @Override
    public void visit(StructExpression expression) {
        write("StructExpression" + expression.getStructName());
        indent++;
        write("Field" + expression.getFieldName());
        indent--;
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        write("IdentifierExpression: " + identifierExpression.getIdentifier());
    }

    @Override
    public void visit(FunctionCallExpression functionCallExpression) {
        write("FunctionCallExpression" + functionCallExpression.getIdentifier());
        indent++;
        functionCallExpression.getArguments().forEach(this::callAccept);
        indent--;
    }

    @Override
    public void visit(LessExpression expression) {
        write("LessExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(GreaterExpression expression) {
        write("GreaterExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(EqualExpression expression) {
        write("EqualExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(NotEqualExpression expression) {
        write("NotEqualExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(LessEqualExpression expression) {
        write("LessEqualExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(GreaterEqualExpression expression) {
        write("GreaterEqualExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(AddArithmeticExpression expression) {
        write("AddArithmeticExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(DivideArithmeticExpression expression) {
        write("DivideArithmeticExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(ModuloArithmeticExpression expression) {
        write("DivideArithmeticExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(MultiplyArithmeticExpression expression) {
        write("DivideArithmeticExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(SubtractArithmeticExpression expression) {
        write("SubtractArithmeticExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(BinaryArithmeticExpression expression) {
        write("DivideArithmeticExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(NegateArithmeticExpression expression) {
        write("NegateArithmeticExpression");
        indent++;
        callAccept(expression.getExpression());
        indent--;
    }

    @Override
    public void visit(AndLogicalExpression expression) {
        write("AndLogicalExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(NegateLogicalExpression expression) {
        write("NegateLogicalExpression");
        indent++;
        callAccept(expression.getExpression());
        indent--;
    }

    @Override
    public void visit(OrLogicalExpression expression) {
        write("AndLogicalExpression");
        indent++;
        callAccept(expression.getLeft());
        callAccept(expression.getRight());
        indent--;
    }

    @Override
    public void visit(UnaryFactorExpression expression) {
        write("UnaryFactorExpression");
        indent++;
        callAccept(expression.getExpression());
        indent--;
    }

    @Override
    public void visit(CastedFactorExpression expression) {
        write("CastedFactorExpression");
        indent++;
        write("Type" + expression.getType());
        callAccept(expression.getExpression());
        indent--;
    }

    @Override
    public void visit(CopiedValueExpression expression) {
        write("CastedFactorExpression");
        indent++;
        callAccept(expression.getExpression());
        indent--;
    }

    private <T extends Node> void callAccept(T expression) {
        currentPosition = expression.getPosition();
        expression.accept(this);
    }

    private void write(String message) {
        out.println(" ".repeat(indent) + message);
    }
}
