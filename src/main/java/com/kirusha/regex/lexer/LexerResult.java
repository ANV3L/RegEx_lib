package com.kirusha.regex.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Report returned by the lexer: tokens + original input.
 */
public final class LexerResult {

    private final List<Token> tokens;
    private final String originalInput;

    /**
     * @param tokens        list of tokens (will be copied and wrapped as unmodifiable)
     * @param originalInput original regex string
     */
    public LexerResult(List<Token> tokens, String originalInput) {
        this.tokens = Collections.unmodifiableList(new ArrayList<>(tokens));
        this.originalInput = originalInput;
    }

    /** @return immutable list of tokens */
    public List<Token> getTokens() {
        return tokens;
    }

    /** @return original regex string */
    public String getOriginalInput() {
        return originalInput;
    }

    /** @return number of tokens */
    public int size() {
        return tokens.size();
    }

    /** @return true if there are no tokens */
    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    @Override
    public String toString() {
        return "LexerResult{input=\"" + originalInput + "\", tokens=" + tokens + "}";
    }
}
