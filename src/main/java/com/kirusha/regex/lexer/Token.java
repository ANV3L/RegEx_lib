package com.kirusha.regex.lexer;

import java.util.Objects;


public final class Token {

    private final TokenType type;
    private final String value;
    private final int position;


    public Token(TokenType type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }


    public TokenType getType() {
        return type;
    }


    public String getValue() {
        return value;
    }


    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return position == token.position &&
                type == token.type &&
                Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, position);
    }

    
    @Override
    public String toString() {
        switch (type) {
            case CHAR:
            case NUMBER:
            case BACKREF:
                return type + "(" + value + ")";
            default:
                return type.toString();
        }
    }
}
