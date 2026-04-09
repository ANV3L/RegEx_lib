package com.kirusha.regex.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lexer {

    public LexerResult tokenize(String input) {
        Objects.requireNonNull(input, "input cannot be null");

        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);

            if (c == '\\') {
                Token token = handleEscape(input, i);
                tokens.add(token);
                i += 2;
            } else if (isMetaCharacter(c)) {
                TokenType type = metaCharToTokenType(c);
                tokens.add(new Token(type, String.valueOf(c), i));
                i += 1;
            } else if (Character.isDigit(c)) {
                Token token = handleNumber(input, i);
                tokens.add(token);
                i += token.getValue().length();
            } else {
                tokens.add(new Token(TokenType.CHAR, String.valueOf(c), i));
                i += 1;
            }
        }

        return new LexerResult(tokens, input);
    }

    private Token handleEscape(String input, int position) {
        if (position + 1 >= input.length()) {
            throw new LexerException("Unexpected end of input after '\\'", position);
        }

        char next = input.charAt(position + 1);
        if (next >= '1' && next <= '9') {
            return new Token(TokenType.BACKREF, String.valueOf(next), position);
        }
        return new Token(TokenType.CHAR, String.valueOf(next), position);
    }

    private Token handleNumber(String input, int position) {
        int idx = position;
        StringBuilder sb = new StringBuilder();
        while (idx < input.length() && Character.isDigit(input.charAt(idx))) {
            sb.append(input.charAt(idx));
            idx++;
        }
        return new Token(TokenType.NUMBER, sb.toString(), position);
    }

    private boolean isMetaCharacter(char c) {
        return c == '|' || c == '*' || c == '(' || c == ')' ||
                c == '[' || c == ']' || c == '{' || c == '}' || c == '#';
    }

    private TokenType metaCharToTokenType(char c) {
        switch (c) {
            case '|':
                return TokenType.PIPE;
            case '*':
                return TokenType.STAR;
            case '(':
                return TokenType.LPAREN;
            case ')':
                return TokenType.RPAREN;
            case '[':
                return TokenType.LBRACKET;
            case ']':
                return TokenType.RBRACKET;
            case '{':
                return TokenType.LBRACE;
            case '}':
                return TokenType.RBRACE;
            case '#':
                return TokenType.EPSILON;
            default:
                throw new IllegalArgumentException("Not a meta character: " + c);
        }
    }
}
