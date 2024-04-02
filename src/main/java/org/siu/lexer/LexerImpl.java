package org.siu.lexer;

import lombok.extern.slf4j.Slf4j;
import org.siu.error.ErrorHandler;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;
import org.siu.token.type.IntegerToken;
import org.siu.token.type.KeywordToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

@Slf4j
public class LexerImpl implements Lexer {
    private BufferedReader reader;
    private Character character;
    private Position position = null;
    private Token token = null;
    private ErrorHandler errorHandler;

    public LexerImpl(String text, ErrorHandler errorHandler) {
        this.reader = new BufferedReader(new StringReader(text));
        this.errorHandler = errorHandler;
    }

    @Override
    public Token nextToken() {
        skipWhiteCharacters();
        if (buildEOF() || buildNumber())
            return token;

        if (character == '\n')
            return null;

        return nextToken();
    }

    private boolean buildEOF() {
        if (Character.toString(character).equals(TokenType.END_OF_FILE.getKeyword())) {
            token = new KeywordToken(TokenType.END_OF_FILE, position);
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

        try {
            while (Character.isDigit(character)) {
                x = character - '0';
                if (result > (Integer.MAX_VALUE - x) / 10) {
                    log.error("Too big integer");
                    throw new Exception("Integer overflow");
                    // TODO: throw lexer error
                } else {

                }
                result = result * 10 + x;
                nextCharacter();
            }
        } catch (Exception e) {
            log.error("buffer overflow");
        }
        token = new IntegerToken(position, result);
    }

    private void nextCharacter() {
        int charNum = 0;
        try {
            charNum = reader.read();
            if (charNum == -1) {
                // TODO: handle end of text
                // end of text
//                return ' ';
                throw new Exception("END OF TEXT");
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
