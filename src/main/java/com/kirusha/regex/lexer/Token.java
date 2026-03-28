package com.kirusha.regex.lexer;

import java.util.Objects;

/**
 * Immutable token: minimal meaningful unit of a regex.
 */
public final class Token {

    private final TokenType type;
    private final String value;
    private final int position;

    /**
     * @param type     token type
     * @param value    string value (char itself, number digits, backref index, or meta symbol)
     * @param position zero-based position of the token's first character in the original regex
     */
    public Token(TokenType type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    /** @return token type */
    public TokenType getType() {
        return type;
    }

    /** @return token value */
    public String getValue() {
        return value;
    }

    /** @return position of the token in the original input */
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

    /**
     * Human-friendly format: CHAR(a)  PIPE  NUMBER(12)
     */
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
