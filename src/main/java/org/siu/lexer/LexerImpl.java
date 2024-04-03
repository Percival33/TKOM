package org.siu.lexer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.siu.error.ErrorHandler;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;
import org.siu.token.type.BooleanToken;
import org.siu.token.type.IntegerToken;
import org.siu.token.type.KeywordToken;
import org.siu.token.type.StringToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import static org.siu.token.TokenType.BOOLEAN_FALSE;
import static org.siu.token.TokenType.BOOLEAN_TRUE;

@Slf4j
public class LexerImpl implements Lexer {
    private final BufferedReader reader;
    private Character character;
    private Position position = null;
    private Token token = null;
    private final ErrorHandler errorHandler;

    public LexerImpl(String text, ErrorHandler errorHandler) {
        this.reader = new BufferedReader(new StringReader(text));
        this.errorHandler = errorHandler;
    }

    @Override
    public Token nextToken() {
        skipWhiteCharacters();
        if (buildEOF() || buildNumber() || buildString() || buildKeyword())
            return token;

        if (character == '\n')
            return null;

        return nextToken();
    }

    private boolean buildKeyword() {
        StringBuilder sb = new StringBuilder();
        String potentialKeyword;
        sb.append(character);
        nextCharacter();
        while(StringUtils.isAlphanumeric(String.valueOf(character))) {
            sb.append(character);
            nextCharacter();
        }

        potentialKeyword = sb.toString();

        if(TokenType.matchBoolean(potentialKeyword)) {
            token = new BooleanToken(null, Boolean.valueOf(potentialKeyword));
            return true;
        }
//        errorHandler.handleLexerError(new Exception("Invalid keyword"));
        return false;
    }

    private boolean buildString() {
        if (!Character.toString(character).equals("\""))  return false;
        processString();
        return true;
    }

    private void processString() {
        StringBuilder sb = new StringBuilder();
        nextCharacter();
        while (character != '"') {
            sb.append(character);
            nextCharacter();
        }
        token = new StringToken(null, sb.toString());
    }

    private boolean buildEOF() {
        if (Character.toString(character).equals(TokenType.END_OF_FILE.getKeyword())) {
            token = new KeywordToken(position, TokenType.END_OF_FILE);
            return true;
        }
        return false;
    }

    // TODO: update position, handle EOF reader returning -1
    private boolean buildNumber() {
        if (!Character.isDigit(character)) return false;
        processNumber();
        return true;
    }

    private void processNumber() {
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
        token = new IntegerToken(position, result);
    }

    private void nextCharacter() {
        int charNum = 0;
        try {
            charNum = reader.read();
            if (charNum == -1) {
                character = '\u0003';
                // TODO: handle end of text
                // end of text
                return;
//                errorHandler.handleLexerError(new Exception("END OF TEXT"));
            }
        } catch (IOException e) {
            log.error(e.toString());
        } catch (Exception e) {
            log.error(e.toString());
//            throw new RuntimeException(e);
        }
        character = (char) charNum;
    }

    private void skipWhiteCharacters() {
        while (true) {
            nextCharacter();
            if (!Character.isWhitespace(character)) break;
        }
    }
}
