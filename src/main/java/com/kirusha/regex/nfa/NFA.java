package com.kirusha.regex.nfa;

import java.util.Set;


public class NFA {

    private final NFAState startState;

    private final NFAState acceptState;

    private final Set<NFAState> states;

    private final Set<String> alphabet;

    private final int groupCount;

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
