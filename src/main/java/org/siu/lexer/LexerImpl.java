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

// TODO: add position, add handling too long identifier
@Slf4j
public class LexerImpl implements Lexer {
    private final BufferedReader reader;
    private String character;
    private Position position = null;
    private final ErrorHandler errorHandler;

    public LexerImpl(String text, ErrorHandler errorHandler) {
        this.reader = new BufferedReader(new StringReader(text));
        this.errorHandler = errorHandler;
        nextCharacter();
    }

    @Override
    public Token nextToken() {
        skipWhiteCharacters();
        Token token = buildEOF()
                .or(() -> buildNumber())
                .or(() -> buildString())
                .or(() -> buildIdentifierOrKeyword())
                .or(() -> buildOperator())
                .orElse(null);

        if (token == null) {
            log.error("Invalid token");
        }

        return token;
    }

    private Optional<Token> buildOperator() {
        StringBuilder sb = new StringBuilder();
        String potentialKeyword;

        while (TokenUtils.isSymbol(String.valueOf(character))) {
            sb.append(character);
            nextCharacter();
        }

        potentialKeyword = sb.toString();

        var tokenType = TokenUtils.OPERATORS.getOrDefault(potentialKeyword, null);
        return tokenType != null ? Optional.of(new KeywordToken(tokenType, null)) : Optional.empty();
    }

    private Optional<Token> buildIdentifierOrKeyword() {
        if (!StringUtils.isAlpha(String.valueOf(character))) return Optional.empty();

        StringBuilder sb = new StringBuilder();
        String identifier;

        while (StringUtils.isAlphanumeric(String.valueOf(character))) {
            sb.append(character);
            nextCharacter();
        }

        identifier = sb.toString();

        var tokenType = TokenUtils.KEYWORDS.getOrDefault(identifier, TokenType.IDENTIFIER);
        if (tokenType == TokenType.BOOLEAN_TRUE || tokenType == TokenType.BOOLEAN_FALSE) {
            return Optional.of(new BooleanToken(null, Boolean.valueOf(identifier)));
        } else if (tokenType != TokenType.IDENTIFIER) {
            return Optional.of(new KeywordToken(tokenType, null));
        }

        return Optional.of(new StringToken(tokenType, null, identifier));
    }

    private Optional<Token> buildString() {
        if (!character.equals("\"")) return Optional.empty();

        StringBuilder sb = new StringBuilder();
        nextCharacter();

        while (!character.equals(TokenUtils.END_OF_FILE)) {
            if (character.equals(LexerConfig.STRING_ENCLOSING_CHARACTER)) {
                if (sb.charAt(sb.length() - 1) != '\\') break;
                sb.deleteCharAt(sb.length() - 1);
            }

            sb.append(character);
            nextCharacter();
        }
        return Optional.of(new StringToken(TokenType.STRING_CONSTANT, null, sb.toString()));
    }

    private Optional<Token> buildEOF() {
        if (character.equals(TokenType.END_OF_FILE.getKeyword())) {
            return Optional.of(new KeywordToken(TokenType.END_OF_FILE, position));
        }
        return Optional.empty();
    }

    // TODO: update position, handle EOF reader returning -1
    private Optional<Token> buildNumber() {
        if (!StringUtils.isNumeric(character)) return Optional.empty();

        int decimal = processNumber();
        if(!character.equals(TokenUtils.DOT)) {
            return Optional.of(new IntegerToken(position, decimal));
        }

        float fractional = processFractional();
        float number = (float)decimal + fractional;

        return Optional.of(new FloatToken(position, number));
    }

    private float processFractional() {
        int result = 0;
        int power = 0;
        nextCharacter();

        while(StringUtils.isNumeric(character)) {
            if(power > LexerConfig.MAX_FRACTIONAL_DIGITS) {
                log.error("Too many fractional digits. Skipping rest digits");
                errorHandler.handleLexerError(new Exception("Too many fractional digits"));
                while (StringUtils.isNumeric(character)) {
                    nextCharacter();
                }
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
                errorHandler.handleLexerError(new Exception("Integer overflow"));
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
        } catch (IOException e) {
            log.error(e.toString());
        }
        character = charNum == -1 ? TokenUtils.END_OF_FILE : Character.toString((char) charNum);
        return character;
    }

    private void skipWhiteCharacters() {
        while (StringUtils.isWhitespace(character) && StringUtils.isNotEmpty(character)) {
            nextCharacter();
        }
    }
}
