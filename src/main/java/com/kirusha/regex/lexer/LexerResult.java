package com.kirusha.regex.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LexerResult {

    private final List<Token> tokens;
    private final String originalInput;


    public LexerResult(List<Token> tokens, String originalInput) {
        this.tokens = Collections.unmodifiableList(new ArrayList<>(tokens));
        this.originalInput = originalInput;
    }

    
    public List<Token> getTokens() {
        return tokens;
    }

    
    public String getOriginalInput() {
        return originalInput;
    }

    
    public int size() {
        return tokens.size();
    }
    
    
    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    @Override
    public String toString() {
        return "LexerResult{input=\"" + originalInput + "\", tokens=" + tokens + "}";
    }
}
