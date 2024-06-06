package org.siu.parser;

import io.vavr.Function3;
import lombok.extern.slf4j.Slf4j;
import org.siu.ast.Parameter;
import org.siu.ast.BlockStatement;
import org.siu.ast.Program;
import org.siu.ast.expression.*;
import org.siu.ast.expression.arithmetic.*;
import org.siu.ast.expression.logical.AndLogicalExpression;
import org.siu.ast.expression.logical.NegateLogicalExpression;
import org.siu.ast.expression.relation.*;
import org.siu.ast.statement.*;
import org.siu.ast.type.*;
import org.siu.error.*;
import org.siu.lexer.Lexer;
import org.siu.ast.expression.logical.OrLogicalExpression;
import org.siu.ast.function.FunctionDefinitionStatement;
import org.siu.ast.Statement;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


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

    private void saveFunctionDefinition(FunctionDefinitionStatement statement, Map<String, FunctionDefinitionStatement> functions) {
        if (functions.containsKey(statement.getName())) {
            handleParserError(new RedefinitionError(functions.get(statement.getName()).getPosition()), statement.getPosition());
        }
        functions.put(statement.getName(), statement);
    }

    private void saveTypeDefinition(Statement statement, Map<String, Statement> typeDefinitions) {
        if (typeDefinitions.containsKey(statement.getName())) {
            handleParserError(new RedefinitionError(typeDefinitions.get(statement.getName()).getPosition()), statement.getPosition());
        }
        typeDefinitions.put(statement.getName(), statement);
    }

    private void saveDeclaration(Statement statement, Map<String, Statement> declarations) {
        String name = statement.getName();

        if (declarations.containsKey(name)) {
            handleParserError(new RedefinitionError(declarations.get(name).getPosition()), statement.getPosition());
        }
        declarations.put(name, statement);
    }

    /**
     * PROGRAM                 = { FN_DEFINITION | DECLARATION | FN_CALL };
     */
    public Program buildProgram() {
        nextToken();
        Map<String, FunctionDefinitionStatement> functions = new HashMap<>();
        Map<String, Statement> declarations = new HashMap<>();
        Map<String, Statement> typeDefinitions = new HashMap<>();

        do {
            var type = parseTypeDefinitionStatement();
            if (type.isPresent()) {
                saveTypeDefinition(type.get(), typeDefinitions);
                continue;
            }

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

            log.error("Unhandled statement at: {}", token.getPosition());
            break;
        } while (token.getType() != TokenType.END_OF_FILE);

        return new Program(functions, declarations, typeDefinitions);
    }

    /**
     * VARIANT_DEFINITION              = "variant", IDENTIFIER, "{", STRUCT_TYPE_DECL, {, ",", STRUCT_TYPE_DECL }, "}";
     * STRUCT_DEFINITION               = "struct", IDENTIFIER, "{", { STRUCT_TYPE_DECL }, "}", ";";
     */
    private Optional<Statement> parseTypeDefinitionStatement() {
        switch (token.getType()) {
            case VARIANT -> {
                nextToken();
                return parseVariantTypeDefinition();
            }
            case STRUCT -> {
                return parseStructTypeDefinition();
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    /**
     * STRUCT_DEFINITION               = IDENTIFIER, "{", { STRUCT_TYPE_DECL }, "}", ";";
     */
    private Optional<Statement> parseStructTypeDefinition() {
        nextToken();
        var position = token.getPosition();
        var name = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.CURLY_BRACKET_OPEN, SyntaxError::new);
        List<Parameter> members = new ArrayList<>();

        var member = parseParameter();

        while (member.isPresent()) {
            members.add(member.get());
            mustBe(token, TokenType.SEMICOLON, SyntaxError::new);
            member = parseParameter();
        }

        mustBe(token, TokenType.CURLY_BRACKET_CLOSE, SyntaxError::new);
        mustBe(token, TokenType.SEMICOLON, SyntaxError::new);

        return Optional.of(new StructTypeDefinitionStatement(name, members, position));
    }

    /**
     * VARIANT_DEFINITION              = IDENTIFIER, "{", STRUCT_TYPE_DECL, {, ",", STRUCT_TYPE_DECL }, "}";
     */
    private Optional<Statement> parseVariantTypeDefinition() {
        var position = token.getPosition();
        var name = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        position = token.getPosition();
        mustBe(token, TokenType.CURLY_BRACKET_OPEN, SyntaxError::new);
        List<Parameter> members = new ArrayList<>();

        var member = parseParameter();
        if (member.isEmpty()) {
            log.error("No member in variant at: {}", position);
            handleParserError(new EmptyVariantException(position), position);
        }
        members.add(member.get());

        while (token.getType() == TokenType.COMMA) {
            nextToken();
            member = parseParameter();
            if (member.isEmpty()) {
                mustBe(token, TokenType.CURLY_BRACKET_CLOSE, SyntaxError::new);
                break;
            }
            members.add(member.get());
        }

        return Optional.of(new VariantTypeDefinitionStatement(name, members, position));
    }

    /**
     * DECLARATION                     = ["const"], VARIABLE_DECLARATION;
     * <p>
     * VARIABLE_DECLARATION    = SIMPLE_TYPE_AS_ARG, IDENTIFIER, "=", EXPRESSION, ";"
     * | IDENTIFIER, IDENTIFIER, "=", EXPRESSION, ";"
     */
    private Optional<Statement> parseDeclarationStatement() {
        var isConst = parseConst();
        var typeDeclarationOptional = parseTypeDeclaration();
        if (typeDeclarationOptional.isEmpty()) {
            if (isConst) {
                log.error("No type declaration in const statement at: {}", token.getPosition());
                handleParserError(new MissingTypeAfterConstException(token.getPosition()), token.getPosition());
            }

            return Optional.empty();
        }

        var typeDeclaration = typeDeclarationOptional.get();
        var position = token.getPosition();
        var identifier = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();

        if (token.getType() != TokenType.ASSIGN) {
            log.error("Invalid declaration statement at: {}", position);
            handleParserError(new SyntaxError(position), position);
        }

        nextToken();
        var expression = parseExpression();
        if (expression.isEmpty()) {
            log.error("No expression in variable declaration at: {}", position);
            handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.SEMICOLON, MissingSemicolonError::new);
        var parameter = new Parameter(typeDeclaration, identifier);
        var declaration = new DeclarationStatement(parameter, expression.get(), position);
        if (isConst) {
            return Optional.of(new ConstStatement(parameter, declaration, position));
        }
        return Optional.of(declaration);
    }

    private boolean parseConst() {
        if (token.getType() == TokenType.CONST) {
            nextToken();
            return true;
        }
        return false;
    }


    private Optional<Expression> parseExpression() {
        return parseLogicExpression();
    }

    private Optional<TypeDeclaration> parseTypeDeclaration() {
        var type = ValueType.of(token.getType());
        if (type.isEmpty()) {
            return Optional.empty();
        }

        if (type.get() == ValueType.CUSTOM) {
            var customType = token.getValue().toString();
            nextToken();
            return Optional.of(new TypeDeclaration(type.get(), customType));
        }

        nextToken();
        return Optional.of(new TypeDeclaration(type.get()));
    }

    private Object mustBe(Token token, TokenType expectedType, Function<Position, ? extends ParserError> errorSupplier) {
        if (token.getType() != expectedType) {
            var error = errorSupplier.apply(token.getPosition());
            log.error("{}. Expected {} got {}", error.toString(), expectedType, token.getType());
            handleParserError(error, error.getPosition());
        }
        var value = token.getValue();
        nextToken();
        return value;
    }

    //    FN_DEFINITION           = "fn", IDENTIFIER, "(", [ FN_PARAMS, { ",", FN_PARAMS }], ")", [":", FN_RET_TYPES], BLOCK;
    private Optional<FunctionDefinitionStatement> parseFunctionDefinition() {
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
            handleParserError(new SyntaxError(position), position);
        }

        return Optional.of(new FunctionDefinitionStatement(name.toString(), params, returnType, block.get(), position));
//        return Optional.empty();
    }

    /**
     * BLOCK                   = "{", { STATEMENT, ";" }, "}";
     */
    private Optional<BlockStatement> parseBlock() {
        mustBe(token, TokenType.CURLY_BRACKET_OPEN, SyntaxError::new);
        var position = token.getPosition();
        List<Statement> statements = new ArrayList<>();

        var statement = parseStatement();
        while (statement.isPresent()) {
            statements.add(statement.get());
            statement = parseStatement();
        }

        mustBe(token, TokenType.CURLY_BRACKET_CLOSE, SyntaxError::new);
        return Optional.of(new BlockStatement(statements, position));
    }

    private final List<Supplier<Optional<? extends Statement>>> statementSuppliers = List.of(
            this::parseIfStatement,
            this::parseWhileStatement,
            this::parseAssignmentOrDeclarationOrExpression,
            this::parseDeclarationStatement,
            this::parseReturnStatement,
            this::parseMatchStatement
    );

    /**
     * MATCH                           = "match", "(", IDENTIFIER, ")", "{", { MATCH_EXP }, "}"
     */
    private Optional<MatchStatement> parseMatchStatement() {
        if (token.getType() != TokenType.MATCH) {
            return Optional.empty();
        }
        var position = token.getPosition();
        nextToken();
        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var identifier = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        mustBe(token, TokenType.CURLY_BRACKET_OPEN, SyntaxError::new);
        List<MatchCaseExpression> matchCases = new ArrayList<>();
        var matchCase = parseMatchCase();
        while (matchCase.isPresent()) {
            matchCases.add(matchCase.get());
            matchCase = parseMatchCase();
        }
        mustBe(token, TokenType.CURLY_BRACKET_CLOSE, SyntaxError::new);
        return Optional.of(new MatchStatement(identifier, matchCases, position));
    }

    /**
     * MATCH_EXP                       = IDENTIFIER, "::", IDENTIFIER, "(", IDENTIFIER, ")", "{" EXPRESSION "}";
     */
    private Optional<MatchCaseExpression> parseMatchCase() {
        if (token.getType() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var position = token.getPosition();
        var variantType = token.getValue().toString();
        nextToken();
        mustBe(token, TokenType.DOUBLE_COLON, SyntaxError::new);
        var member = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var variable = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        mustBe(token, TokenType.CURLY_BRACKET_OPEN, SyntaxError::new);
        var expression = parseExpression();
        if (expression.isEmpty()) {
            log.error("No expression in match case at: {}", position);
            handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.CURLY_BRACKET_CLOSE, SyntaxError::new);
        return Optional.of(new MatchCaseExpression(variantType, member, variable, expression.get(), position));
    }

    /**
     * ASSINGMENT                      = IDENTIFIER, "=", EXPRESSION
     * | IDENTIFIER, ".", IDENTIFIER, "=", EXPRESSION
     * | IDENTIFIER, "=", IDENTIFIER, "::", IDENTIFIER, "(", EXPRESSION ")"; (* variant *)
     */
    private Optional<Statement> parseAssignmentOrDeclarationOrExpression() {
        if (token.getType() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var name = token.getValue().toString();
        var position = token.getPosition();

        nextToken();

        var statement = parseAssignmentStatement(name, position)
                .or(() -> parseFnCallStatement(name, position));
        if (statement.isPresent()) return statement;

        var variable = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.ASSIGN, SyntaxError::new);
        var expression = parseExpression();

        if (expression.isEmpty()) {
            log.error("No expression in declaration at: {}", position);
            handleParserError(new MissingExpressionError(position), position);
        }

        var type = new TypeDeclaration(ValueType.CUSTOM, name);
        mustBe(token, TokenType.SEMICOLON, MissingSemicolonError::new);

        return Optional.of(new DeclarationStatement(new Parameter(type, variable), expression.get(), position));
    }

    private Optional<Statement> parseFnCallStatement(String name, Position position) {
        if (token.getType() != TokenType.BRACKET_OPEN) {
            return Optional.empty();
        }

        var arguments = parseFnArguments();

        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        mustBe(token, TokenType.SEMICOLON, SyntaxError::new);

        return Optional.of(new FunctionCallExpression(name, arguments, position));
    }

    private Optional<Statement> parseAssignmentStatement(String name, Position position) {
        if(!(token.getType() == TokenType.ASSIGN || token.getType() == TokenType.DOT)) {
            return Optional.empty();
        }
        var statement = parseAssignmentStatement(name);
        if (statement.isEmpty()) {
            log.error("No statement in assignment at: {}", position);
            handleParserError(new SyntaxError(position), position);
        }
        mustBe(token, TokenType.SEMICOLON, SyntaxError::new);
        return Optional.of(statement.get());
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
    private Optional<Statement> parseAssignmentStatement(String name) {
        var position = token.getPosition();

        var member = parseStructMemberAssignmentStatement(name, position);
        if (member.isPresent()) {
            return member;
        }

        mustBe(token, TokenType.ASSIGN, SyntaxError::new);
        var expression = parseExpression();

        if (expression.isEmpty()) {
            log.error("No expression in assignment at: {}", position);
            handleParserError(new MissingExpressionError(position), position);
        }

        if (token.getType() == TokenType.DOUBLE_COLON) {
            return parseVariantAssignment(name, position);
        }

        return Optional.of(new AssignmentStatement(name, expression.get(), position));
    }

    private Optional<Statement> parseStructMemberAssignmentStatement(String structName, Position position) {
        if(token.getType() != TokenType.DOT) {
            return Optional.empty();
        }

        nextToken();
        var fieldName = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.ASSIGN, SyntaxError::new);
        var expression = parseExpression();
        if (expression.isEmpty()) {
            log.error("No expression in assignment at: {}", position);
            handleParserError(new MissingExpressionError(position), position);
        }
        return Optional.of(new StructMemberAssignmentStatement(new StructExpression(structName, fieldName, position), expression.get(), position));
    }

    /**
     * "::", IDENTIFIER, "(", EXPRESSION ")"; (* variant *)
     * part of variant assignment
     */
    private Optional<Statement> parseVariantAssignment(String name, Position position) {
        nextToken();
        var variantName = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var variantExpression = parseExpression();
        if (variantExpression.isEmpty()) {
            log.error("No expression in assignment at: {}", position);
            handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        return Optional.of(new AssignmentStatement(name + "::" + variantName, variantExpression.get(), position));
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
            handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        var block = parseBlock();
        if (block.isEmpty()) {
            log.error("Block cannot be empty at: {}", position);
            handleParserError(new SyntaxError(position), position);
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
                handleParserError(new SyntaxError(position), position);
            }
            conditions.add(elifCondition.get());
            ifInstructions.add(elifBlock.get());
        }

        if (token.getType() == TokenType.ELSE) {
            nextToken();
            var elseBlock = parseBlock();
            if (elseBlock.isEmpty()) {
                log.error("Block cannot be empty at: {}", position);
                handleParserError(new SyntaxError(position), position);
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
            handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        var block = parseBlock();
        if (block.isEmpty()) {
            log.error("Block cannot be empty at: {}", position);
            handleParserError(new SyntaxError(position), position);
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
    private List<Parameter> parseFunctionParameters() {
        List<Parameter> parameters = new ArrayList<>();
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
                handleParserError(new SyntaxError(token.getPosition()), token.getPosition());
            }
            parameters.add(param.get());
        }
        return parameters;
    }

    /**
     * FN_PARAMS               = SIMPLE_TYPE_AS_ARG
     * | STRUCT_OR_VARIANT_AS_ARG
     */
    private Optional<Parameter> parseParameter() {
        var type = parseTypeDeclaration();
        if (type.isEmpty()) {
            return Optional.empty();
        }
        var identifier = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();

        if (identifier.isEmpty()) {
            log.error("Invalid parameter at: {}", token.getPosition());
            handleParserError(new SyntaxError(token.getPosition()), token.getPosition());
            return Optional.empty();
        }
        return Optional.of(new Parameter(type.get(), identifier));
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
                handleParserError(new SyntaxError(position, "No expression after OR."), position);
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
                handleParserError(new SyntaxError(token.getPosition(), "No expression after AND."), token.getPosition());
            }
            left = new AndLogicalExpression(left, right.get(), position);
        }
        return Optional.of(left);
    }

    private final Map<TokenType, Function3<Expression, Expression, Position, Expression>> relationOperator = Map.of(
            TokenType.LESS, Function3.of(LessExpression::new),
            TokenType.LESS_EQUAL, Function3.of(LessEqualExpression::new),
            TokenType.GREATER, Function3.of(GreaterExpression::new),
            TokenType.GREATER_EQUAL, Function3.of(GreaterEqualExpression::new),
            TokenType.EQUAL, Function3.of(EqualExpression::new),
            TokenType.NOT_EQUAL, Function3.of(NotEqualExpression::new)
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
            handleParserError(new SyntaxError(relationPosition, "No expression after relation operator."), relationPosition);
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
        var castedType = Optional.<TypeDeclaration>empty();

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

    private Optional<TypeDeclaration> parseCastedSimpleType() {
        return ValueType.of(token.getType())
                .filter(valueType -> valueType != ValueType.CUSTOM)
                .map(TypeDeclaration::new)
                .or(() -> {
                    log.warn("Invalid cast syntax at: {}", token.getPosition());
                    return Optional.empty();
                });
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
        var factorOptional = parseStructDefinitionExpression()
                .or(this::parseLiteralExpression)
                .or(this::parseIdentifierOrFnCallOrVariant)
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

    /**
     * IDENTIFIER_FNCALL_MEM_VARIANT   = IDENTIFIER,
     * [
     * ".", IDENTIFIER,
     * | "(", [ FN_ARGUMENTS ], ")",
     * | "::", IDENTIFIER, "(", EXPRESSION ")"
     * ]
     */
    private Optional<Expression> parseIdentifierOrFnCallOrVariant() {
        if (token.getType() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var name = token.getValue().toString();
        var position = token.getPosition();
        nextToken();

        return parseStructExpression(name, position)
                .or(() -> parseFnCallExpression(name, position))
                .or(() -> parseVariantExpression(name, position))
                .or(() -> Optional.of(new IdentifierExpression(name, position)));
    }

    private Optional<Expression> parseStructExpression(String name, Position position) {
        if(token.getType() != TokenType.DOT) return Optional.empty();

        nextToken();
        var fieldName = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        return Optional.of(new StructExpression(name, fieldName, position));
    }

    private Optional<Expression> parseFnCallExpression(String name, Position position) {
        if(token.getType() != TokenType.BRACKET_OPEN) return Optional.empty();

        var arguments = parseFnArguments();
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        return Optional.of(new FunctionCallExpression(name, arguments, position));
    }

    private Optional<Expression> parseVariantExpression(String name, Position position) {
        if(token.getType() != TokenType.DOUBLE_COLON) return Optional.empty();

        nextToken();

        var fieldName = mustBe(token, TokenType.IDENTIFIER, SyntaxError::new).toString();
        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var variantExpression = parseExpression();

        if (variantExpression.isEmpty()) {
            log.error("No expression in variant at: {}", position);
            handleParserError(new MissingExpressionError(position), position);
        }
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        return Optional.of(new VariantExpression(fieldName, variantExpression.get(), position));
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
            copy = parseCopyOperator();
            var expressionOptional = parseExpression();
            if (expressionOptional.isEmpty()) {
                if (copy) {
                    handleParserError(new SyntaxError(token.getPosition(), "No expression after copy operator."), token.getPosition());
                }
                break;
            }
            expression = expressionOptional.get();
            if (copy) {
                expression = new CopiedValueExpression(expression, expression.getPosition());
            }
            arguments.add(expression);
        } while (token.getType() == TokenType.COMMA);
        return arguments;
    }

    /**
     * '{', EXPRESSION, { ',', EXPRESSION }, '}' ( * struct definition expression *)
     */
    private Optional<Expression> parseStructDefinitionExpression() {
        if (token.getType() != TokenType.CURLY_BRACKET_OPEN) {
            return Optional.empty();
        }
        nextToken();

        var arguments = new ArrayList<Expression>();
        while (token.getType() != TokenType.CURLY_BRACKET_CLOSE) {
            var expression = parseExpression();
            if (expression.isEmpty()) {
                log.error("No expression in struct at: {}", token.getPosition());
                handleParserError(new MissingExpressionError(token.getPosition()), token.getPosition());
            }
            arguments.add(expression.get());
            if (token.getType() == TokenType.COMMA) {
                nextToken();
            }
        }

        mustBe(token, TokenType.CURLY_BRACKET_CLOSE, SyntaxError::new);

        return Optional.of(new StructDeclarationExpression(arguments, token.getPosition()));
    }

    private boolean parseCopyOperator() {
        if (token.getType() == TokenType.COPY_OPERATOR) {
            nextToken();
            return true;
        }
        return false;
    }

    private void handleParserError(ParserError error, Position position) {
        throw new RuntimeException(error.toString() + " at: " + position);
    }
}
