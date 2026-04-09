package com.kirusha.regex.lexer;

public class LexerException extends RuntimeException {

    private final int position;

    public LexerException(String message, int position) {
        super(message);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "LexerException at position " + position + ": " + getMessage();
    }
}
