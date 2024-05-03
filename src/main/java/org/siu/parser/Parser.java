package org.siu.parser;

import lombok.extern.slf4j.Slf4j;
import org.siu.ast.Block;
import org.siu.ast.Program;
import org.siu.ast.ProgramElement;
import org.siu.ast.expression.relation.Less;
import org.siu.error.ErrorHandler;
import org.siu.error.ParserError;
import org.siu.error.RedefinitionError;
import org.siu.error.SyntaxError;
import org.siu.lexer.Lexer;
import org.siu.ast.expression.Expression;
import org.siu.ast.expression.logical.OrExpression;
import org.siu.ast.function.FunctionDefinition;
import org.siu.ast.function.FunctionParameter;
import org.siu.ast.statement.Statement;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import java.io.BufferedReader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: implement parseMatch, ??

@Slf4j
public class Parser {
    private final Lexer lexer;
    private Token token;


    public Parser(BufferedReader reader, ErrorHandler errorHandler) {
        this.lexer = new LexerImpl(reader, errorHandler);
    }

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        token = lexer.nextToken();
    }

    private Token consumeToken() {
        this.token = lexer.nextToken();
        return this.token;
    }

    //    PROGRAM                 = { FN_DEFINITION | DECLARATION | FN_CALL};
    public Program buildProgram() throws ParserError {
        Map<String, ProgramElement> functions = new HashMap<>();
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
        while ((funDef = parseFunDef()) != null) {
            try {
                if (functions.containsKey(funDef.getName())) {
                    throw new RedefinitionError(functions.get(funDef.getName()).getPosition());
                }
                functions.put(funDef.getName(), funDef);
//            } catch (SyntaxError e) {
//                log.error("syntax error: ", e.getPosition());
//                log.error(e.toString());
            } catch (RedefinitionError e) {
                log.error("redefinition error: ", e.getPosition());
                log.error(e.toString());
            } catch (Exception e) {
                log.error("something went horribly wrong. {}", e.toString());
            }
        }
        lexer.nextToken();
        return new Program(functions);
    }

    private Object mustBe(Token token, TokenType type, Function<Position, ? extends ParserError> errorSupplier) throws ParserError {
        // TODO: rename function
        if (token.getType() != type) {
            var error = errorSupplier.apply(token.getPosition());
            log.error(error.toString());
            throw error;
        }
        var value = token.getValue();
        consumeToken();
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
        token = consumeToken();
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
    // TODO: wizytator z statement


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
            consumeToken();
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

//    IDENTIFIER_FNCALL_MEM   = IDENTIFIER, [ ( ".", IDENTIFIER | [ "(", [ FN_ARGUMENTS ], ")" ] ) ];
    private Statement parseIdentifierOrFnCall() throws ParserError {
        // TODO: implement this function
        if(token.getType() != TokenType.IDENTIFIER) {
            return null;
        }
        var name = token.getValue().toString();
        var position = token.getPosition();
        consumeToken();
        Statement statement;
        if ((statement = parseFnCall(name, position)) != null) {

        }
        else {
            if((statement = parseAssignment(name)) == null)
                throw new SyntaxError(position);
        }
        mustBe(token, TokenType.SEMICOLON,SyntaxError::new);
        return statement;
    }

    private Statement parseAssignment(String name) {
        return null;
    }

    //    IDENTIFIER, "(", [ FN_ARGUMENTS ], ")"
    private Statement parseFnCall(String name, Position position) {
        // TODO: implement this function
        if(token.getType() != TokenType.BRACKET_OPEN)
            return null;
        return null;
    }

    private Expression parseConditionalExpression() throws SyntaxError {
        Expression left = parseAndExpression();
        if(left == null) return null;
        while(token.getType() == TokenType.OR) {
            var position = token.getPosition();
            consumeToken();
            var right_logic_factor = parseAndExpression();
            if(right_logic_factor == null) {
                throw new SyntaxError(position, "No expression after OR.");
            }
            left = new OrExpression(left, position, right_logic_factor);
        }
        return left;
    }

    private Expression parseAndExpression() {
        var left = parseRelationExpression();
        // TODO: remember if was negated.
        // TODO: return factor or unary_negate(factor) as an expression
        while(token.getType() == TokenType.AND)
            parseAndExpression();
        return null;
    }

    private Expression parseRelationExpression() {
        var left = parseMathExpression();
        Map<TokenType, Supplier<Expression>> relationOperator = new HashMap<>();
        relationOperator.put(TokenType.LESS, Less::new);
        while(relationOperator.containsKey(token.getValue())) {
            Supplier<Expression> supplier = relationOperator.get(token.getType());
            if (supplier != null) {
                Expression expression = supplier.get();  // Create a new instance
                // some code...
            }
        }
        return null;
    }

    private Expression parseMathExpression() {
        // var left = parseTerm / multiplicaationExpression
        return null;
    }


    public Expression addExpression() {

    }

}
