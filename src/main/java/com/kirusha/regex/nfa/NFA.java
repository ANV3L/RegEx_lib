package com.kirusha.regex.nfa;

import java.util.Set;

/**
 * Полное представление недетерминированного конечного автомата.
 *
 * На первом этапе удобно считать, что у автомата:
 * - одно стартовое состояние;
 * - одно принимающее состояние;
 * - множество всех состояний;
 * - множество символов алфавита.
 *
 * При необходимости позже можно расширить:
 * - хранить несколько accept-state;
 * - хранить информацию о группах захвата.
 */
public class NFA {

    /**
     * Стартовое состояние автомата.
     */
    private final NFAState startState;

    /**
     * Единственное принимающее состояние автомата.
     */
    private final NFAState acceptState;

    /**
     * Множество всех состояний автомата.
     */
    private final Set<NFAState> states;

    /**
     * Алфавит автомата — множество всех символов,
     * встречающихся в символьных переходах.
     */
    private final Set<String> alphabet;

    /**
     * Количество групп захвата, пришедшее из ParserResult.
     * На первом этапе может просто сохраняться "на будущее".
     */
    private final int groupCount;

    /**
     * @param startState стартовое состояние
     * @param acceptState принимающее состояние
     * @param states все состояния
     * @param alphabet алфавит
     * @param groupCount количество групп захвата
     */
    public NFA(NFAState startState,
               NFAState acceptState,
               Set<NFAState> states,
               Set<String> alphabet,
               int groupCount) {
        this.startState = startState;
        this.acceptState = acceptState;
        this.states = states;
        this.alphabet = alphabet;
        this.groupCount = groupCount;
    }

    public NFAState getStartState() {
        return startState;
    }

    public NFAState getAcceptState() {
        return acceptState;
    }

    public Set<NFAState> getStates() {
        return states;
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }

    public int getGroupCount() {
        return groupCount;
    }

    /**
     * Удобный вывод для debug.
     */
    @Override
    public String toString() {
        return "NFA{" +
                "start=" + startState +
                ", accept=" + acceptState +
                ", states=" + states.size() +
                ", alphabet=" + alphabet +
                ", groupCount=" + groupCount +
                '}';
    }
}
