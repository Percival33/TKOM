package org.siu.token;

public interface Token {
    TokenType getType();
    Position getPosition();
    <T> T getValue();
}
