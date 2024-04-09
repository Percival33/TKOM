package org.siu.lexer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.siu.error.ErrorHandler;
import org.siu.token.Position;
import org.siu.token.Token;
import org.siu.token.TokenType;
import org.siu.token.TokenUtils;
import org.siu.token.type.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

@Slf4j
public class LexerImpl implements Lexer {
    private final BufferedReader reader;
    private String character;
    private Position position;
    private Position tokenPosition;
    private final ErrorHandler errorHandler;

    public LexerImpl(String text, ErrorHandler errorHandler) {
        this.reader = new BufferedReader(new StringReader(text));
        this.errorHandler = errorHandler;
        this.position = new Position();
        nextCharacter();
    }

    @Override
    public Token nextToken() {
        skipWhiteCharacters();
        tokenPosition = position.copy();
        Token token = buildEOF()
                .or(() -> buildNumber())
                .or(() -> buildString())
                .or(() -> buildIdentifierOrKeyword())
                .or(() -> buildOperatorOrSymbol())
                .orElse(null);

        if (token == null) {
            log.error("Invalid token");
            errorHandler.handleLexerError(new Exception("Invalid token"), tokenPosition);
        }

        return token;
    }

    private Optional<Token> buildOperatorOrSymbol() {
        StringBuilder sb = new StringBuilder();
        String potentialKeyword;

        while (TokenUtils.isSymbol(String.valueOf(character))) {
            sb.append(character);
            nextCharacter();
        }

        potentialKeyword = sb.toString();

        var tokenType = TokenUtils.OPERATORS.getOrDefault(potentialKeyword, null);
        if (tokenType == null) {
            tokenType = TokenUtils.SYMBOLS.getOrDefault(potentialKeyword, null);
        }
        return tokenType != null ? Optional.of(new KeywordToken(tokenType, tokenPosition)) : Optional.empty();
    }

    private Optional<Token> buildIdentifierOrKeyword() {
        if (!StringUtils.isAlpha(String.valueOf(character))) return Optional.empty();

        StringBuilder sb = new StringBuilder();
        String identifier;

        while (StringUtils.isAlphanumeric(String.valueOf(character))) {
            if(sb.length() == LexerConfig.MAX_IDENTIFIER_LENGTH) {
                log.error("Too long identifier. Skipping rest characters");
                errorHandler.handleLexerError(new Exception("Too long identifier"), tokenPosition);
                while (StringUtils.isAlphanumeric(String.valueOf(character))) {
                    nextCharacter();
                }
                break;
            }
            sb.append(character);
            nextCharacter();
        }

        identifier = sb.toString();

        var tokenType = TokenUtils.KEYWORDS.getOrDefault(identifier, TokenType.IDENTIFIER);
        if (TokenUtils.BOOLEANS.get(identifier) != null) {
            return Optional.of(new BooleanToken(tokenPosition, Boolean.valueOf(identifier)));
        } else if (tokenType != TokenType.IDENTIFIER) {
            return Optional.of(new KeywordToken(tokenType, tokenPosition));
        }

        return Optional.of(new StringToken(tokenType, tokenPosition, identifier));
    }

    private Optional<Token> buildString() {
        if (!character.equals(LexerConfig.STRING_ENCLOSING_CHARACTER)) return Optional.empty();

        StringBuilder sb = new StringBuilder();
        nextCharacter();

        while (!character.equals(TokenUtils.END_OF_FILE)) {
            if (character.equals(LexerConfig.STRING_ENCLOSING_CHARACTER)) {
                if (!String.valueOf(sb.charAt(sb.length() - 1)).equals(LexerConfig.ESCAPE_SYMBOL)) break;
                sb.deleteCharAt(sb.length() - 1);
            }

            sb.append(character);
            nextCharacter();
        }
        return Optional.of(new StringToken(TokenType.STRING_CONSTANT, tokenPosition, sb.toString()));
    }

    private Optional<Token> buildEOF() {
        if (character.equals(TokenType.END_OF_FILE.getKeyword())) {
            return Optional.of(new KeywordToken(TokenType.END_OF_FILE, tokenPosition));
        }
        return Optional.empty();
    }

    private Optional<Token> buildNumber() {
        if (!StringUtils.isNumeric(character)) return Optional.empty();

        int decimal = processNumber();
        if(!character.equals(TokenUtils.DOT)) {
            return Optional.of(new IntegerToken(tokenPosition, decimal));
        }

        float fractional = processFractional();
        float number = (float)decimal + fractional;

        return Optional.of(new FloatToken(tokenPosition, number));
    }

    private float processFractional() {
        int result = 0;
        int power = 0;
        nextCharacter();

        while(StringUtils.isNumeric(character)) {
            if(power >= LexerConfig.MAX_FRACTIONAL_DIGITS) {
                log.error("Too many fractional digits. Skipping rest digits");
                errorHandler.handleLexerError(new Exception("Too many fractional digits"), tokenPosition);
                while (StringUtils.isNumeric(character)) {
                    nextCharacter();
                }
                break;
            }
            result = result * 10 + (character.charAt(0) - '0');
            power++;
            nextCharacter();
        }
        return (float)result / (float)Math.pow(10, power);
    }

    private int processNumber() {
        int result = character.charAt(0) - '0';
        int x;
        nextCharacter();

        while (StringUtils.isNumeric(character)) {
            x = character.charAt(0) - '0';
            if (result > (Integer.MAX_VALUE - x) / 10) {
                log.error("integer overflow. Skipping rest digits");
                errorHandler.handleLexerError(new Exception("Integer overflow"), tokenPosition);
                while (StringUtils.isNumeric(character)) {
                    nextCharacter();
                }
                break;
            }
            result = result * 10 + x;
            nextCharacter();
        }
        return result;
    }

    private String nextCharacter() {
        int charNum = 0;
        try {
            charNum = reader.read();
            position.nextCharacter();
        } catch (IOException e) {
            log.error(e.toString());
        }
        character = charNum == -1 ? TokenUtils.END_OF_FILE : Character.toString((char) charNum);
        if (character.equals(LexerConfig.LINE_BREAK)) position.nextLine();
        return character;
    }

    private void skipWhiteCharacters() {
        while (StringUtils.isWhitespace(character) && StringUtils.isNotEmpty(character)) {
            nextCharacter();
        }
    }
}
