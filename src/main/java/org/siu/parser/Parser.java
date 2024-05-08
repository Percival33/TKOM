package org.siu.parser;

import io.vavr.Function3;
import lombok.extern.slf4j.Slf4j;
import org.siu.ast.Argument;
import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.AndLogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.relation.EqualExpression;
import org.siu.ast.expression.relation.GreaterExpression;
import org.siu.ast.expression.relation.LessExpression;
import org.siu.ast.statement.*;
import org.siu.ast.type.*;
import org.siu.error.*;
import org.siu.lexer.Lexer;
import org.siu.ast.expression.logical.OrLogicalExpression;
import org.siu.ast.function.FunctionDefinition;
import org.siu.ast.Statement;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: implement parseMatch, ??

@Slf4j
public class Parser {
    private final ErrorHandler errorHandler;
    private final Lexer lexer;
    private Token token;

    public Parser(Lexer lexer, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.lexer = lexer;
    }

    private Token nextToken() {
        this.token = lexer.nextToken();
        return this.token;
    }

    private void saveFunctionDefinition(FunctionDefinition statement, Map<String, FunctionDefinition> functions) {
        if (functions.containsKey(statement.getName())) {
            errorHandler.handleParserError(new RedefinitionError(functions.get(statement.getName()).getPosition()), statement.getPosition());
        }
        functions.put(statement.getName(), statement);
    }

    private void saveDeclaration(DeclarationStatement statement, Map<String, DeclarationStatement> declarations) {
        if (declarations.containsKey(statement.getArgument().getName())) {
            errorHandler.handleParserError(new RedefinitionError(declarations.get(statement.getArgument().getName()).getPosition()), statement.getPosition());
        }
        declarations.put(statement.getArgument().getName(), statement);
    }

    //    PROGRAM                 = { FN_DEFINITION | DECLARATION | FN_CALL };
    public Program buildProgram() {
        nextToken();
        Map<String, FunctionDefinition> functions = new HashMap<>();
        Map<String, DeclarationStatement> declarations = new HashMap<>();

        var parse = true;
        do {
            var declaration = parseDeclarationStatement();
            if (declaration.isPresent()) {
                saveDeclaration(declaration.get(), declarations);
                continue;
            }
            var funDef = parseFunctionDefinition();
            if (funDef.isPresent()) {
                saveFunctionDefinition(funDef.get(), functions);
                continue;
            }
            if (token.getType() == TokenType.SEMICOLON) {
                continue;
            }
            break;
        } while (parse && token.getType() != TokenType.END_OF_FILE);

        return new Program(functions, declarations);
    }

    /**
     * VARIABLE_DECLARATION    = SIMPLE_TYPE_AS_ARG, IDENTIFIER, "=", EXPRESSION, ";"
     * | IDENTIFIER, IDENTIFIER, "=", EXPRESSION, ";"
     * | IDENTIFIER, IDENTIFIER, "=", "{", STRUCT_MEMBER, { ",", STRUCT_MEMBER }, "}", ";"
     * | IDENTIFIER, IDENTIFIER, "=", IDENTIFIER, "::", IDENTIFIER, "(", EXPRESSION, ")", ";" ; (* variant *)
     */
    private Optional<DeclarationStatement> parseDeclarationStatement() {
        var typeDeclarationOptional = parseTypeDeclaration();
        if (typeDeclarationOptional.isEmpty()) {
            return Optional.empty();
        }
        var typeDeclaration = typeDeclarationOptional.get();
        var position = token.getPosition();
        var identifier = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.ASSIGN, SyntaxError::new);
        // TODO: can be { or Identifier

        var expression = parseExpression();
        if (expression.isEmpty()) {
            // TODO: refactor it to generic function
            errorHandler.handleParserError(new MissingExpressionError(position), position);
        }

        mustBe(token, TokenType.SEMICOLON, MissingSemicolonError::new);
        return Optional.of(new DeclarationStatement(new Argument(typeDeclaration, identifier), expression.get(), position));
    }

    private Optional<Expression> parseExpression() {
        return parseLogicExpression();
    }

    private Optional<TypeDeclaration> parseTypeDeclaration() {
        var type = ValueType.of(token.getType());
        if (type.isEmpty()) {
            return Optional.empty();
        }
        nextToken();
        return Optional.of(new TypeDeclaration(type.get()));
    }

    private Object mustBe(Token token, TokenType expectedType, Function<Position, ? extends ParserError> errorSupplier) {
        if (token.getType() != expectedType) {
            var error = errorSupplier.apply(token.getPosition());
            log.error("{}. Expected {} got {}", error.toString(), expectedType, token.getType());
            errorHandler.handleParserError(error, error.getPosition());
        }
        var value = token.getValue();
        nextToken();
        return value;
    }

    //    FN_DEFINITION           = "fn", IDENTIFIER, "(", [ FN_PARAMS, { ",", FN_PARAMS }], ")", [":", FN_RET_TYPES], BLOCK;
    private Optional<FunctionDefinition> parseFunctionDefinition() {
        if (token.getType() != TokenType.FUNCTION) {
            return Optional.empty();
        }
        var position = token.getPosition();
        token = nextToken();
        var name = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new);

        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var params = parseFunctionParameters();
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);

        var returnType = parseReturnType();
        var block = parseBlock();
        if (block.isEmpty()) {
            log.error("Block cannot be empty at: {}", position);
            errorHandler.handleParserError(new SyntaxError(position), position);
        }

        return Optional.of(new FunctionDefinition(name.toString(), params, returnType, block.get(), position));
//        return Optional.empty();
    }

    /**
     * BLOCK                   = "{", { STATEMENT, ";" }, "}";
     */
    private Optional<BlockStatement> parseBlock() {
        mustBe(token, TokenType.SQUARE_BRACKET_OPEN, SyntaxError::new);
        var position = token.getPosition();
        List<Statement> statements = new ArrayList<>();

        var statement = parseStatement();
        while (statement.isPresent()) {
            statements.add(statement.get());
            statement = parseStatement();
        }

        mustBe(token, TokenType.SQUARE_BRACKET_CLOSE, SyntaxError::new);
        return Optional.of(new BlockStatement(statements, position));
    }

    private final List<Supplier<Optional<? extends Statement>>> statementSuppliers = List.of(
            this::parseIfStatement,
            this::parseWhileStatement,
            this::parseAssignmentOrDeclarationOrExpression,
            this::parseDeclarationStatement,
//            this::parseAssignmentStatementOrSingleExpression,
            this::parseReturnStatement
    );

    private Optional<Statement> parseAssignmentOrDeclarationOrExpression() {
        if (token.getType() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var name = token.getValue().toString();
        var position = token.getPosition();
        nextToken();
        if (token.getType() == TokenType.ASSIGN || token.getType() == TokenType.DOT) {
            var statement = parseAssignmentStatement(name);
            if (statement.isEmpty()) {
                log.error("No statement in assignment at: {}", position);
                errorHandler.handleParserError(new SyntaxError(position), position);
            }
            return Optional.of((Statement) statement.get());
        }

        if(token.getType() == TokenType.BRACKET_OPEN) {
            var arguments = parseFnArguments();
            mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
            return Optional.of(new FunctionCallExpression(name, arguments, position));
        }

        var variable = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.ASSIGN, SyntaxError::new);
        var expression = parseExpression();
        if (expression.isEmpty()) {
            log.error("No expression in assignment at: {}", position);
            errorHandler.handleParserError(new MissingExpressionError(position), position);
        }
        var type = new TypeDeclaration(ValueType.CUSTOM, name);
        return Optional.of(new DeclarationStatement(new Argument(type, variable), expression.get(), position));
    }


    private Optional<Statement> parseStatement() {
        for (var supplier : statementSuppliers) {
            var statement = supplier.get();
            if (statement.isPresent()) {
                return (Optional<Statement>) statement;
            }
        }
        return Optional.empty();
    }

    /**
     * ASSINGMENT              = IDENTIFIER, "=", EXPRESSION
     * | IDENTIFIER, "=", IDENTIFIER, "::", IDENTIFIER, "(", EXPRESSION ")"; (* variant *)
     * | IDENTIFIER, ".", IDENTIFIER, "=", EXPRESSION
     */
    private Optional<AssignmentStatement> parseAssignmentStatement(String name) {
        var position = token.getPosition();

        if (token.getType() == TokenType.DOT) {
            nextToken();
            var fieldName = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
            mustBe(token, TokenType.ASSIGN, SyntaxError::new);
            nextToken();
            var expression = parseExpression();
            if (expression.isEmpty()) {
                log.error("No expression in assignment at: {}", position);
                errorHandler.handleParserError(new MissingExpressionError(position), position);
            }
            return Optional.of(new AssignmentStatement(name+"."+fieldName, expression.get(), position));
        }
        // FIXME: expr albo identifier
        mustBe(token, TokenType.ASSIGN, SyntaxError::new);
        var expression = parseExpression();
        if (expression.isEmpty()) {
            log.error("No expression in assignment at: {}", position);
            errorHandler.handleParserError(new MissingExpressionError(position), position);
        }

        if (token.getType() == TokenType.DOUBLE_COLON) {
            return parseVariantAssignment(name, position);
        }

        return Optional.of(new AssignmentStatement(name, expression.get(), position));
    }

    private Optional<AssignmentStatement> parseVariantAssignment(String name, Position position) {
        nextToken();
        var variantName = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var variantExpression = parseExpression();
        if (variantExpression.isEmpty()) {
            log.error("No expression in assignment at: {}", position);
            errorHandler.handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        return Optional.of(new AssignmentStatement(name+"::"+variantName, variantExpression.get(), position));
    }

    /**
     * IF_STATEMENT            = "if", "(", EXPRESSION, ")", BLOCK,
     * { "elif", "(", EXPRESSION, ")", BLOCK, },
     * [ "else", BLOCK ];
     */
    private Optional<IfStatement> parseIfStatement() {
        if (token.getType() != TokenType.IF) {
            return Optional.empty();
        }

        var conditions = new ArrayList<Expression>();
        var ifInstructions = new ArrayList<BlockStatement>();
        var elseInstructions = Optional.<BlockStatement>empty();

        var position = token.getPosition();
        nextToken();
        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var condition = parseExpression();
        if (condition.isEmpty()) {
            log.error("No condition in if statement at: {}", position);
            errorHandler.handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        var block = parseBlock();
        if (block.isEmpty()) {
            log.error("Block cannot be empty at: {}", position);
            errorHandler.handleParserError(new SyntaxError(position), position);
        }

        conditions.add(condition.get());
        ifInstructions.add(block.get());


        while (token.getType() == TokenType.ELIF) {
            nextToken();
            mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
            var elifCondition = parseExpression();
            mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
            var elifBlock = parseBlock();
            if (elifBlock.isEmpty()) {
                log.error("Block cannot be empty at: {}", position);
                errorHandler.handleParserError(new SyntaxError(position), position);
            }
            conditions.add(elifCondition.get());
            ifInstructions.add(elifBlock.get());
        }

        if (token.getType() == TokenType.ELSE) {
            nextToken();
            var elseBlock = parseBlock();
            if (elseBlock.isEmpty()) {
                log.error("Block cannot be empty at: {}", position);
                errorHandler.handleParserError(new SyntaxError(position), position);
            }
            elseInstructions = Optional.of(elseBlock.get());
        }
        return Optional.of(new IfStatement(conditions, ifInstructions, elseInstructions, position));
    }

    /**
     * WHILE_STATEMENT         = "while", "(", EXPRESSION, ")", BLOCK;
     */
    private Optional<WhileStatement> parseWhileStatement() {
        if (token.getType() != TokenType.WHILE) {
            return Optional.empty();
        }
        var position = token.getPosition();
        nextToken();
        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var condition = parseExpression();
        if (condition.isEmpty()) {
            log.error("No condition in while statement at: {}", position);
            errorHandler.handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        var block = parseBlock();
        if (block.isEmpty()) {
            log.error("Block cannot be empty at: {}", position);
            errorHandler.handleParserError(new SyntaxError(position), position);
        }
        return Optional.of(new WhileStatement(condition.get(), block.get(), position));
    }

    /**
     * RETURN_STATEMENT        = "return", EXPRESSION, ";"
     * | "return", ";";
     */
    private Optional<ReturnStatement> parseReturnStatement() {
        if (token.getType() != TokenType.RETURN) {
            return Optional.empty();
        }
        var position = token.getPosition();
        nextToken();

        var expression = parseExpression();
        mustBe(token, TokenType.SEMICOLON, MissingSemicolonError::new);

        return Optional.of(new ReturnStatement(expression.orElse(null), position));
    }

    private Optional<TypeDeclaration> parseReturnType() {
        if (token.getType() != TokenType.COLON) {
            return Optional.empty();
        }
        nextToken();
        return parseTypeDeclaration();
    }

    /**
     * [ FN_PARAMS, { ",", FN_PARAMS }],
     */
    private List<Argument> parseFunctionParameters() {
        List<Argument> parameters = new ArrayList<>();
        var param = parseParameter();
        if (param.isEmpty()) {
            log.info("Empty parameters list at: {}", token.getPosition());
            return parameters;
        }
        parameters.add(param.get());
        while (token.getType() == TokenType.COMMA) {
            nextToken();
            param = parseParameter();
            if (param.isEmpty()) {
                log.error("");
                errorHandler.handleParserError(new SyntaxError(token.getPosition()), token.getPosition());
            }
            parameters.add(param.get());
        }
        return parameters;
    }

    /**
     * FN_PARAMS               = SIMPLE_TYPE_AS_ARG
     * | STRUCT_OR_VARIANT_AS_ARG
     */
    private Optional<Argument> parseParameter() {
        var type = parseTypeDeclaration();
        if (type.isEmpty()) {
            return Optional.empty();
        }
        var identifier = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();

        if (type.isEmpty() || identifier.isEmpty()) {
            log.error("Invalid parameter at: {}", token.getPosition());
            errorHandler.handleParserError(new SyntaxError(token.getPosition()), token.getPosition());
            return Optional.empty();
        }
        return Optional.of(new Argument(type.get(), identifier));
    }

    private Optional<Expression> parseLogicExpression() {
        var leftOptional = parseAndExpression();
        if (leftOptional.isEmpty()) return Optional.empty();
        var left = leftOptional.get();
        while (token.getType() == TokenType.OR) {
            nextToken();
            var position = token.getPosition();
            var right = parseAndExpression();
            if (right.isEmpty()) {
                errorHandler.handleParserError(new SyntaxError(position, "No expression after OR."), position);
            }
            left = new OrLogicalExpression(left, right.get(), position);
        }
        return Optional.of(left);
    }

    private Optional<Expression> parseAndExpression() {
        var leftOptional = parseRelationExpression();
        if (leftOptional.isEmpty()) return Optional.empty();
        var left = leftOptional.get();

        while (token.getType() == TokenType.AND) {
            nextToken();
            var position = token.getPosition();
            var right = parseAndExpression();
            if (right.isEmpty()) {
                errorHandler.handleParserError(new SyntaxError(token.getPosition(), "No expression after AND."), token.getPosition());
            }
            left = new AndLogicalExpression(left, right.get(), position);
        }
        return Optional.of(left);
    }

    private final Map<TokenType, Function3<Expression, Expression, Position, Expression>> relationOperator = Map.of(
            TokenType.LESS, Function3.of(LessExpression::new),
//            TokenType.LESS_EQUAL, LessEqual::new,
            TokenType.GREATER, Function3.of(GreaterExpression::new),
//            TokenType.GREATER_EQUAL, GreaterEqual::new,
            TokenType.EQUAL, Function3.of(EqualExpression::new)
//            TokenType.NOT_EQUAL, NotEqual::new
    );

    /**
     * RELATION_EXPRESSION     = ["not"], MATH_EXPRESSION, [ relation_operator, MATH_EXPRESSION ];
     */
    private Optional<Expression> parseRelationExpression() {
        var negate = isNegated();
        var left = parseMathExpression();
        if (left.isEmpty()) return Optional.empty();
        var type = token.getType();
        if (!relationOperator.containsKey(type))
            return negate ? Optional.of(new NegateLogicalExpression(left.get(), token.getPosition())) : left;

        var constructor = relationOperator.get(type);
        var relationPosition = token.getPosition();
        nextToken();

        var right = parseMathExpression();
        if (right.isEmpty()) {
            errorHandler.handleParserError(new SyntaxError(relationPosition, "No expression after relation operator."), relationPosition);
            assert false;
        }
        var expression = constructor.apply(left.get(), right.get(), relationPosition);
        if (negate)
            return Optional.of(new NegateLogicalExpression(expression, relationPosition));

        return Optional.of(expression);
    }

    private boolean isNegated() {
        if (token.getType() == TokenType.NOT) {
            nextToken();
            return true;
        }
        return false;
    }

    /**
     * MATH_EXPRESSION         = TERM, { arithmetic_operator, TERM };
     */
    private Optional<Expression> parseMathExpression() {
        var leftOptional = parseTerm();
        if (leftOptional.isEmpty()) {
            return Optional.empty();
        }

        var left = leftOptional.get();
        while (token.getType() == TokenType.PLUS || token.getType() == TokenType.MINUS) {
            var operator = token.getType();
            var position = token.getPosition();
            nextToken();

            var rightOptional = parseTerm();

            if (rightOptional.isPresent()) {
                Expression right = rightOptional.get();
                left = (operator == TokenType.PLUS)
                        ? new AddArithmeticExpression(left, right, position)
                        : new SubtractArithmeticExpression(left, right, position);
            } else {
                break;
            }
        }

        return Optional.of(left);
    }

    private Expression buildArithmeticExpression(Expression left, Expression right, TokenType operationType, Position position) {
        return switch (operationType) {
            case MULTIPLY -> new MultiplyArithmeticExpression(left, right, position);
            case DIVIDE -> new DivideArithmeticExpression(left, right, position);
            case MODULO -> new ModuloArithmeticExpression(left, right, position);
            default -> null;
        };
    }

    /**
     * TERM                    = UNARY_FACTOR, { multiplication_operator, UNARY_FACTOR };
     */
    private Optional<Expression> parseTerm() {
        Optional<Expression> leftOptional = parseFactor();
        if (leftOptional.isEmpty()) {
            return Optional.empty();
        }

        Expression left = leftOptional.get();
        while (token.getType() == TokenType.MULTIPLY || token.getType() == TokenType.DIVIDE || token.getType() == TokenType.MODULO) {
            Position position = token.getPosition();
            TokenType operationType = token.getType();
            nextToken();
            Optional<Expression> rightOptional = parseFactor();
            if (rightOptional.isPresent()) {
                left = buildArithmeticExpression(left, rightOptional.get(), operationType, position);
            }
        }

        return Optional.of(left);
    }

    /**
     * CASTED_FACTOR           = [ "(", SIMPLE_TYPE, ")" ], UNARY_FACTOR;
     * <p>
     * UNARY_FACTOR            = ["-"], FACTOR;
     * <p>
     * FACTOR                  = LITERAL
     * | '(', EXPRESSION, ')'
     * | IDENTIFIER_FNCALL_MEM;
     */
    private Optional<Expression> parseFactor() {
        var position = token.getPosition();
        var factorOptional = Optional.<Expression>empty();
        var castedType = Optional.<ValueType>empty();

        // CASTED_FACTOR | '(', EXPRESSION, ')'
        if (token.getType() == TokenType.BRACKET_OPEN) {
            nextToken();
            castedType = parseCastedSimpleType();
            if (castedType.isPresent()) {
                nextToken();
                mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
                factorOptional = parseFactor();
                return Optional.of(new CastedFactorExpression(castedType.get(), factorOptional.get(), position));
            } else {
                factorOptional = parseExpression();
                mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
                return factorOptional;
            }
        }
        return parseUnaryFactor();
    }

    private Optional<ValueType> parseCastedSimpleType() {
        var castedType = ValueType.of(token.getType());
        if (castedType.isEmpty()) {
            log.warn("Invalid cast syntax at: {}", token.getPosition());
        }

        return castedType.get() == ValueType.CUSTOM ? Optional.empty() : castedType;
    }

    /**
     * UNARY_FACTOR            = ["-"], FACTOR;
     * <p>
     * FACTOR                  = LITERAL
     * | '(', EXPRESSION, ')'
     * | IDENTIFIER_FNCALL_MEM;
     */
    private boolean isUnaryFactor() {
        if (token.getType() == TokenType.MINUS) {
            nextToken();
            return true;
        }
        return false;
    }

    private Optional<Expression> parseUnaryFactor() {
        // UNARY_FACTOR
        var isUnaryFactor = isUnaryFactor();
        var position = token.getPosition();

        // FACTOR
        var factorOptional = parseLiteralExpression()
                .or(this::parseIdentifierOrFnCall)
                .or(Optional::empty);

        if (factorOptional.isEmpty()) {
            return Optional.empty();
        }

        return isUnaryFactor ? Optional.of(new NegateArithmeticExpression(factorOptional.get(), position)) : factorOptional;
    }

    private Optional<Expression> parseLiteralExpression() {
        Optional<Expression> ret;
        switch (token.getType()) {
            case INTEGER_CONSTANT -> ret = Optional.of(new IntegerExpression(token.getValue(), token.getPosition()));
            case FLOAT_CONSTANT -> ret = Optional.of(new FloatExpression(token.getValue(), token.getPosition()));
            case STRING_CONSTANT -> ret = Optional.of(new StringExpression(token.getValue(), token.getPosition()));
            case BOOLEAN_CONSTANT -> ret = Optional.of(new BooleanExpression(token.getValue(), token.getPosition()));
            default -> ret = Optional.empty();
        }
        if (ret.isPresent()) nextToken();
        return ret;
    }

    //    IDENTIFIER_FNCALL_MEM   = IDENTIFIER, [ ( ".", IDENTIFIER | [ "(", [ FN_ARGUMENTS ], ")" ] ) ];
    private Optional<Expression> parseIdentifierOrFnCall() {
        if (token.getType() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var name = token.getValue().toString();
        var position = token.getPosition();
        nextToken();
        Expression expr;

        switch (token.getType()) {
            case DOT -> {
                nextToken();
                var fieldName = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
                expr = new StructExpression(name, fieldName, position);
            }
            case BRACKET_OPEN -> {
                var arguments = parseFnArguments();
                mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
                expr = new FunctionCallExpression(name, arguments, position);
            }
            default -> {
                expr = new IdentifierExpression(name, position);
            }
        }
        return Optional.of(expr);
    }

    /**
     * FN_ARGUMENTS            = ["@"] EXPRESSION, { "," ["@"], EXPRESSION };
     */
    private List<Expression> parseFnArguments() {
        List<Expression> arguments = new ArrayList<>();
        boolean copy;
        Expression expression;

        do {
            nextToken();
            copy = false;
            if (token.getType() == TokenType.COPY_OPERATOR) {
                copy = true;
                nextToken();
            }
            var expressionOptional = parseExpression();
            if (expressionOptional.isEmpty()) {
                if (copy) {
                    errorHandler.handleParserError(new SyntaxError(token.getPosition(), "No expression after copy operator."), token.getPosition());
                }
                break;
            }
            expression = expressionOptional.get();
            if (copy) {
                expression = new CopiedFactorExpression(expression, expression.getPosition());
            }
            arguments.add(expression);
        } while (token.getType() == TokenType.COMMA);
        return arguments;
    }
}
