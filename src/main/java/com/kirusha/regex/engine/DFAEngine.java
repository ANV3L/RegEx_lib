package com.kirusha.regex.engine;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAState;

import java.util.Collections;

/**
 * Матчинг строки через DFA.
 */
public class DFAEngine {

    /**
     * Проверяет, соответствует ли строка DFA.
     *
     * @param dfa скомпилированный DFA
     * @param input входная строка
     * @return true если строка принимается автоматом
     */
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

    /**
     * Проверяет соответствие и возвращает MatchResult (без групп захвата, кроме 0-й).
     *
     * @param dfa скомпилированный DFA
     * @param input входная строка
     * @return MatchResult
     */
    public MatchResult match(DFA dfa, String input) {
        boolean matched = matches(dfa, input);
        if (matched) {
            return new MatchResult(true, Collections.singletonList(input));
        }
        return MatchResult.noMatch();
    }
}
