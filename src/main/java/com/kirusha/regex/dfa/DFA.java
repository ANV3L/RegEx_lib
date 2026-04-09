package com.kirusha.regex.dfa;

import java.util.Set;

public class DFA {

    private final DFAState startState;

    private final Set<DFAState> acceptStates;

    private final Set<DFAState> states;

    private final Set<String> alphabet;

    public DFA(DFAState startState,
               Set<DFAState> acceptStates,
               Set<DFAState> states,
               Set<String> alphabet) {
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

    public boolean isComplete() {
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
