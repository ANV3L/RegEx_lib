package com.kirusha.regex.engine;

import java.util.Collections;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAState;

public class DFAEngine {

    public boolean matches(DFA dfa, String input) {
        DFAState current = dfa.getStartState();
        for (int i = 0; i < input.length(); i++) {
            String symbol = String.valueOf(input.charAt(i));
            current = current.getTransition(symbol);
            if (current == null) {
                return false;
            }
        }
        return current.isAccepting();
    }

    public MatchResult match(DFA dfa, String input) {
        boolean matched = matches(dfa, input);
        if (matched) {
            return new MatchResult(true, Collections.singletonList(input));
        }
        return MatchResult.noMatch();
    }
}
