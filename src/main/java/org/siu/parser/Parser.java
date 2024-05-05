package org.siu.parser;

import io.vavr.Function3;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.siu.ast.Argument;
import org.siu.ast.Block;
import org.siu.ast.Program;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.AndExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.relation.LessExpression;
import org.siu.ast.statement.DeclarationStatement;
import org.siu.ast.type.*;
import org.siu.error.*;
import org.siu.lexer.Lexer;
import org.siu.ast.expression.logical.OrExpression;
import org.siu.ast.function.FunctionDefinition;
import org.siu.ast.function.FunctionParameter;
import org.siu.ast.statement.Statement;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import java.util.*;
import java.util.function.Function;

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

    //    PROGRAM                 = { FN_DEFINITION | DECLARATION | FN_CALL };
    public Program buildProgram() {
        nextToken();
        Map<String, FunctionDefinition> functions = new HashMap<>();
        Map<String, DeclarationStatement> declarations = new HashMap<>();
        FunctionDefinition funDef;
        // TODO: should buildProgram look like this?
        /**
         * Token token = buildEOF()
         *                 .or(() -> buildNumber())
         *                 .or(() -> buildString())
         *                 .or(() -> buildIdentifierOrKeyword())
         *                 .or(() -> buildOperatorOrSymbol())
         *                 .orElse(null);
         */

        var statement = parseDeclarationStatement();

//        while ((funDef = parseFunDef()) != null) {
//            try {
//                if (functions.containsKey(funDef.getName())) {
//                    throw new RedefinitionError(functions.get(funDef.getName()).getPosition());
//                }
//                functions.put(funDef.getName(), funDef);
////            } catch (SyntaxError e) {
////                log.error("syntax error: ", e.getPosition());
////                log.error(e.toString());
//            } catch (RedefinitionError e) {
//                log.error("redefinition error: ", e.getPosition());
//                log.error(e.toString());
//            } catch (Exception e) {
//                log.error("something went horribly wrong. {}", e.toString());
//            }
//        }

        declarations.put(statement.get().getArgument().getName(), statement.get());
        return new Program(functions, declarations);
    }

    /**
     * VARIABLE_DECLARATION    = SIMPLE_TYPE_AS_ARG, IDENTIFIER, "=", EXPRESSION, ";"
     * | IDENTIFIER, IDENTIFIER, "=", EXPRESSION, ";"
     * | IDENTIFIER, IDENTIFIER, "=", "{", STRUCT_MEMBER, { ",", STRUCT_MEMBER }, "}", ";"
     * | IDENTIFIER, IDENTIFIER, "=", IDENTIFIER, "::", IDENTIFIER, "(", EXPRESSION, ")", ";" ; (* variant *)
     */
    private Optional<DeclarationStatement> parseDeclarationStatement() {
        var typeDeclaration = parseTypeDeclaration();
        if (typeDeclaration.isEmpty()) {
            return Optional.empty();
        }

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
        return Optional.of(new DeclarationStatement(new Argument(typeDeclaration.get(), identifier), expression.get(), position));
    }

    private Optional<Expression> parseExpression() {
        return parseLogicExpression();
    }

    private Optional<ValueType> parseTypeDeclaration() {
        var type = ValueType.of(token.getType());
        if (type.isEmpty()) {
            return Optional.empty();
        }
        nextToken();
        return type;
    }

    private Object mustBe(Token token, TokenType expectedType, Function<Position, ? extends ParserError> errorSupplier) {
        // TODO: rename function
        if (token.getType() != expectedType) {
            var error = errorSupplier.apply(token.getPosition());
            log.error("{}. Expected {} got {}", error.toString(), expectedType, token.getType());
            errorHandler.handleParserError(error, error.getPosition());
        }
        var value = token.getValue();
        nextToken();
        return value;
//        return token.getValue();
    }

//    private <T> T mustBe(Token token, TokenType type, Class<T> clazz, Function<Position, ? extends ParserError> errorSupplier) throws ParserError {
//        // TODO: rename function
//        if (token.getType() != type) {
//            var error = errorSupplier.apply(token.getPosition());
//            log.error(error.toString());
//            throw error;
//        }
//        try {
//            return clazz.cast(token.getValue());
//        } catch (ClassCastException e) {
//            throw new IllegalArgumentException("Token value is not of the expected type: " + clazz.getSimpleName(), e);
//        }
//    }


    //    FN_DEFINITION           = "fn", IDENTIFIER, "(", [ FN_PARAMS, { ",", FN_PARAMS }], ")", [":", FN_RET_TYPES], BLOCK;
    private FunctionDefinition parseFunDef() throws ParserError {
        if (token.getType() != TokenType.FUNCTION) {
            return null;
        }
        var position = token.getPosition(); // FIXME: should it be position.copy?
        token = nextToken();
        mustBe(token, TokenType.IDENTIFIER, SyntaxError::new);
//        mustBe(token, TokenType.IDENTIFIER, String.class, SyntaxError::new);
        var name = token.getValue();

        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var params = parseParameters();

        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);

        var returnType = parseReturnType();
        var block = parseBlock();
        if (block.isEmpty()) {
            log.error("Block cannot be empty at: {}", position);
            throw new SyntaxError(position);
        }

        return new FunctionDefinition(name.toString(), params, returnType, block.get(), position);
    }


    private Optional<Block> parseBlock() {
        return Optional.empty();
    }

    private Optional<FunctionParameter> parseReturnType() {
        // TODO: change type of optional
        return Optional.empty();
    }

    private List<FunctionParameter> parseParameters() throws SyntaxError {
        List<FunctionParameter> parameters = new ArrayList<>();
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
                throw new SyntaxError(token.getPosition());
            }
            parameters.add(param.get());
        }
        return parameters;
    }

    private Optional<FunctionParameter> parseParameter() {
//        TODO: implement function
//        consumeToken();
        return Optional.empty();
    }

    private Statement parseAssignment(String name) {
        return null;
    }

    private Optional<Expression> parseLogicExpression() {
        var leftOptional = parseAndExpression();
        if (leftOptional.isEmpty()) return Optional.empty();
        var left = leftOptional.get();
        while (token.getType() == TokenType.OR) {
            var position = token.getPosition();
            nextToken();
            var right_logic_factor = parseAndExpression();
            if (right_logic_factor.isEmpty()) {
                // TODO: refactor error handling
                errorHandler.handleParserError(new SyntaxError(position, "No expression after OR."), position);
            }
            left = new OrExpression(left, right_logic_factor.get(), position);
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
            left = new AndExpression(left, right.get(), position);
        }
        return Optional.of(left);
    }

    private final Map<TokenType, Function3<Expression, Expression, Position, Expression>> relationOperator = Map.of(
            TokenType.LESS, Function3.of(LessExpression::new)
//            TokenType.LESS_EQUAL, LessEqual::new,
//            TokenType.GREATER, Greater::new,
//            TokenType.GREATER_EQUAL, GreaterEqual::new,
//            TokenType.EQUAL, Equal::new,
//            TokenType.NOT_EQUAL, NotEqual::new
    );

    /**
     * RELATION_EXPRESSION     = ["not"], MATH_EXPRESSION, { relation_operator, MATH_EXPRESSION };
     */
    private Optional<Expression> parseRelationExpression() {
        var negate = false;
        if (token.getType() == TokenType.NOT) {
            nextToken();
            negate = true;
        }
        var left = parseMathExpression();
        if (left.isEmpty()) return Optional.empty();
        var type = token.getType();
        if (!relationOperator.containsKey(type)) return negate ? Optional.of(new NegateLogicalExpression(left.get(), token.getPosition())) : left;

        var constructor = relationOperator.get(type);
        var relationPosition = token.getPosition();
        nextToken();

        var right = parseMathExpression();
        if (right.isEmpty()) {
            errorHandler.handleParserError(new SyntaxError(relationPosition, "No expression after relation operator."), relationPosition);
            assert false;
        }
        var expression = constructor.apply(left.get(), right.get(), relationPosition);
        return Optional.of(negate ? expression : new NegateLogicalExpression(expression, relationPosition));
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
        while (token.getType() != TokenType.END_OF_FILE) {
            var position = token.getPosition();
            var type = token.getType();
            switch (type) {
                case PLUS -> {
                    nextToken();

                    var rightOptional = parseTerm();
                    if (rightOptional.isPresent()) {
                        left = new AddArithmeticExpression(left, rightOptional.get(), position);
                    }
                }
                case MINUS -> {
                    nextToken();

                    var rightOptional = parseTerm();
                    if (rightOptional.isPresent()) {
                        left = new SubtractArithmeticExpression(left, rightOptional.get(), position);
                    }
                }
                default -> {
                    break;
                }
            }
            if (token.getType() != TokenType.PLUS && token.getType() != TokenType.MINUS) {
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
            default -> throw new IllegalArgumentException("Unsupported operation: " + operationType);
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
     * UNARY_FACTOR            = ["-"], CASTED_FACTOR;
     * <p>
     * CASTED_FACTOR           = [ "(", SIMPLE_TYPE, ")" ], FACTOR;
     * <p>
     * FACTOR                  = LITERAL
     * | '(', EXPRESSION, ')'
     * | IDENTIFIER_FNCALL_MEM;
     */
    // FIXME: remove SneakyThrows
    @SneakyThrows
    private Optional<Expression> parseFactor() {
        var position = token.getPosition();

        // UNARY_FACTOR
        var negate = token.getType() == TokenType.MINUS;
        if (negate) {
            // TODO: implement unary negate Expression
            nextToken();
        }

        Expression castedType = null;
        // CASTED_FACTOR
        if (token.getType() == TokenType.BRACKET_OPEN) {
            nextToken();
            var castedTypeOptional = parseSimpleTypeExpression();
            if (castedTypeOptional.isEmpty()) {
                log.error("Invalid cast syntax at: {}", position);
                errorHandler.handleParserError(new SyntaxError(position), position);
            }
            castedType = castedTypeOptional.get();
            nextToken(); // TODO: make sure that token is valid?
            mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        }

        // FACTOR
        var factor = parseLiteralExpression()
                .or(this::parseIdentifierOrFnCall)
                .or(this::parseExpression)
                .or(Optional::empty);
        if (castedType != null) {
            throw new SyntaxError(position, "Casted factor not implemented.");
            // TODO: use CastedFactorExpression
        }
        return Optional.of(factor.get());
    }

    private Optional<Expression> parseSimpleTypeExpression() {
        Optional<Expression> ret;
        switch (token.getType()) {
            case INT -> ret = Optional.of(new IntegerExpression(token.getValue(), token.getPosition()));
            case FLOAT -> ret = Optional.of(new FloatExpression(token.getValue(), token.getPosition()));
            case STRING -> ret = Optional.of(new StringExpression(token.getValue(), token.getPosition()));
            case BOOL -> ret = Optional.of(new BooleanExpression(token.getValue(), token.getPosition()));
            default -> ret = Optional.empty();
        }
        return ret;
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
        if(ret.isPresent()) nextToken();
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
    // FIXME: remove SneakyThrows
    @SneakyThrows
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
                    throw new SyntaxError(token.getPosition(), "No expression after copy operator.");
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
