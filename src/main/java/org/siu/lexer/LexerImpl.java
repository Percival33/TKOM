package org.siu.lexer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.siu.error.ErrorHandler;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;
import org.siu.token.TokenUtils;
import org.siu.token.type.BooleanToken;
import org.siu.token.type.IntegerToken;
import org.siu.token.type.KeywordToken;
import org.siu.token.type.StringToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

@Slf4j
public class LexerImpl implements Lexer {
    private final BufferedReader reader;
    private Character character;
    private Position position = null;
    private final ErrorHandler errorHandler;

    public LexerImpl(String text, ErrorHandler errorHandler) {
        this.reader = new BufferedReader(new StringReader(text));
        this.errorHandler = errorHandler;
        nextCharacter();
    }

    @Override
    public Token nextToken() {
        Token token;
        skipWhiteCharacters();
        if ((token = buildEOF()) != null)
            return token;
        if ((token = buildNumber()) != null)
            return token;

        if ((token = buildString()) != null)
            return token;

        if ((token = buildIdentifierOrKeyword()) != null)
            return token;

        if ((token = buildOperator()) != null)
            return token;

        if (token == null) {
            log.error("Invalid token");
            return null;
        }

        return nextToken();
    }

    private Token buildOperator() {
        StringBuilder sb = new StringBuilder();
        String potentialKeyword;

        while(TokenUtils.isSymbol(String.valueOf(character))) {
            sb.append(character);
            nextCharacter();
        }

        potentialKeyword = sb.toString();

        var tokenType = TokenUtils.OPERATORS.getOrDefault(potentialKeyword, null);
        return tokenType != null ? new KeywordToken(tokenType, null) : null;
    }

    // "int" -> Keyword(TokenType.INT, null)
    private Token buildIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        String potentialKeyword;

//        !StringUtils.isBlank(String.valueOf(character))s
        while(StringUtils.isAlphanumeric(String.valueOf(character))) {
            sb.append(character);
            nextCharacter();
        }

        potentialKeyword = sb.toString();

        var tokenType = TokenUtils.KEYWORDS.getOrDefault(potentialKeyword, null);
        if (tokenType == TokenType.BOOLEAN_TRUE || tokenType == TokenType.BOOLEAN_FALSE) {
            return new BooleanToken(null, Boolean.valueOf(potentialKeyword));
        } else if(tokenType != null) {
            return new KeywordToken(tokenType, null);
        }

        return null;
    }

    private Token buildString() {
        // TODO: escape " character
        if (!Character.toString(character).equals("\""))  return null;
        StringBuilder sb = new StringBuilder();
        nextCharacter();
        while (character != '"') {
            sb.append(character);
            nextCharacter();
        }
        return new StringToken(TokenType.STRING, null, sb.toString());
    }

    private Token buildEOF() {
        if (Character.toString(character).equals(TokenType.END_OF_FILE.getKeyword())) {
            return new KeywordToken(TokenType.END_OF_FILE, position);
        }
        return null;
    }

    // TODO: update position, handle EOF reader returning -1
    private Token buildNumber() {
        if (!Character.isDigit(character)) return null;
        return processNumber();
    }

    private Token processNumber() {
        int result = Character.getNumericValue(character);
        int x;
        nextCharacter();

        while (Character.isDigit(character)) {
            x = character - '0';
            if (result > (Integer.MAX_VALUE - x) / 10) {
                log.error("integer overflow. Skipping rest digits");
                errorHandler.handleLexerError(new Exception("Integer overflow"));
                while(Character.isDigit(character)) {nextCharacter();}
                break;
            }
            result = result * 10 + x;
            nextCharacter();
        }
        return new IntegerToken(position, result);
    }

    private Character nextCharacter() {
        int charNum = 0;
        try {
            charNum = reader.read();
        } catch (IOException e) {
            log.error(e.toString());
        } catch (Exception e) {
            log.error(e.toString());
//            throw new RuntimeException(e);
        }
        character = charNum == -1 ? '\u0003' : (char)charNum;
        return character;
    }

    private void skipWhiteCharacters() {
        while (Character.isWhitespace(character)) {
            nextCharacter();
        }
    }
}
