package com.kirusha.regex.parser;

public class ParserException extends RuntimeException {

    private final int position;

    public ParserException(String message, int position) {
        super(message);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "ParserException at position " + position + ": " + getMessage();
    }
}
