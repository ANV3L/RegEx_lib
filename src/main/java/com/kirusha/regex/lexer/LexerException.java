package com.kirusha.regex.lexer;

/**
 * Thrown by the lexer on invalid input.
 */
public class LexerException extends RuntimeException {

    private final int position;

    /**
     * @param message  human-readable error description
     * @param position index in the original string where the error occurred
     */
    public LexerException(String message, int position) {
        super(message);
        this.position = position;
    }

    /** @return position of the error */
    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "LexerException at position " + position + ": " + getMessage();
    }
}
