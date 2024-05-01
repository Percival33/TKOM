package org.siu.parser;

import lombok.extern.slf4j.Slf4j;
import org.siu.error.ParserError;
import org.siu.error.RedefinitionError;
import org.siu.error.SyntaxError;
import org.siu.lexer.Lexer;
import org.siu.parser.function.FunctionDefinition;
import org.siu.parser.function.FunctionParameter;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class Parser {
    private final Lexer lexer;
    private Token token;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        token = lexer.nextToken();
    }

    //    PROGRAM                 = { FN_DEFINITION | DECLARATION | FN_CALL};
    public Program buildProgram() throws ParserError {
        Map<String, ProgramElement> functions = new HashMap<>();
        FunctionDefinition funDef;
        while ((funDef = parseFunDef()) != null) {
            try {
                if (functions.containsKey(funDef.getName())) {
                    throw new RedefinitionError(functions.get(funDef.getName()).getPosition());
                }
                functions.put(funDef.getName(), funDef);
            } catch (RedefinitionError e) {
                log.error("semantic error: function redefinition", e.getPosition());
            }
        }
        lexer.nextToken();
        return new Program(functions);
    }

    private void mustBe(Token token, TokenType type, Function<Position, ? extends ParserError> errorSupplier) throws ParserError {
        // TODO: rename function
        if (token.getType() != type) {
            var error = errorSupplier.apply(token.getPosition());
            log.error(error.toString());
            throw error;
        }
    }

//    FN_DEFINITION           = "fn", IDENTIFIER, "(", [ FN_PARAMS, { ",", FN_PARAMS }], ")", [":", FN_RET_TYPES], BLOCK;
    private FunctionDefinition parseFunDef() throws ParserError {
        if (token.getType() != TokenType.FUNCTION) {
            return null;
        }
        var position = token.getPosition();
        token = lexer.nextToken();
        mustBe(token, TokenType.IDENTIFIER, SyntaxError::new);
        var name = token.getValue();
        token = lexer.nextToken();
        mustBe(token, TokenType.BRACKET_OPEN, SyntaxError::new);
        var params = parseParameters();
        token = lexer.nextToken();
        mustBe(token, TokenType.BRACKET_CLOSE, SyntaxError::new);
        var returnType = parseReturnType();

        var block = parseBlock();

        return new FunctionDefinition(name.toString(), params, returnType, block, position);
    }
    // TODO: wizytator z statement


    private Block parseBlock() {
        return null;
    }

    private Optional<FunctionParameter> parseReturnType() {
        // TODO: change type of optional
        return Optional.empty();
    }

    private List<FunctionParameter> parseParameters() {
        return List.of();
    }
}
