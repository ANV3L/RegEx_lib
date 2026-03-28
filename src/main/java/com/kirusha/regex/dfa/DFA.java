package com.kirusha.regex.dfa;

import java.util.Set;

/**
 * Полное представление детерминированного конечного автомата.
 *
 * Отличия от NFA:
 * - переходы детерминированы (один переход на символ);
 * - может быть несколько принимающих состояний;
 * - нет epsilon-переходов.
 */
public class DFA {

    /**
     * Стартовое состояние автомата.
     */
    private final DFAState startState;

    /**
     * Множество принимающих состояний.
     */
    private final Set<DFAState> acceptStates;

    /**
     * Множество всех состояний автомата.
     */
    private final Set<DFAState> states;

    /**
     * Алфавит автомата.
     */
    private final Set<String> alphabet;

    /**
     * @param startState стартовое состояние
     * @param acceptStates принимающие состояния
     * @param states все состояния
     * @param alphabet алфавит
     */
    public DFA(DFAState startState,
               Set<DFAState> acceptStates,
               Set<DFAState> states,
               Set<String> alphabet) {
        // TODO: валидация аргументов
        this.startState = startState;
        this.acceptStates = acceptStates;
        this.states = states;
        this.alphabet = alphabet;
    }

    public DFAState getStartState() {
        return startState;
    }

    public Set<DFAState> getAcceptStates() {
        return acceptStates;
    }

    public Set<DFAState> getStates() {
        return states;
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }

    /**
     * Проверяет, является ли автомат полным:
     * каждое состояние имеет переход по каждому символу алфавита.
     *
     * @return true если DFA полный
     */
    public boolean isComplete() {
        // TODO: реализовать проверку полноты
        for (DFAState state : states) {
            for (String symbol : alphabet) {
                if (state.getTransition(symbol) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "DFA{" +
                "start=" + startState +
                ", acceptStates=" + acceptStates.size() +
                ", states=" + states.size() +
                ", alphabet=" + alphabet +
                '}';
    }
}
