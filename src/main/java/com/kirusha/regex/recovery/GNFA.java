package com.kirusha.regex.recovery;

import java.util.Map;
import java.util.Set;


public class GNFA {

    private final Set<Integer> states;

    private final Map<Long, String> transitions;

    private final int startState;

    private final int acceptState;

    public GNFA(Set<Integer> states, Map<Long, String> transitions,
                int startState, int acceptState) {
        this.states = states;
        this.transitions = transitions;
        this.startState = startState;
        this.acceptState = acceptState;
    }

    public static long encodePair(int from, int to) {
        return ((long) from << 32) | (to & 0xFFFFFFFFL);
    }

    public String getTransition(int from, int to) {
        return transitions.get(encodePair(from, to));
    }

    public void setTransition(int from, int to, String regex) {
        transitions.put(encodePair(from, to), regex);
    }

    public void removeTransition(int from, int to) {
        transitions.remove(encodePair(from, to));
    }

    public void removeState(int state) {
        states.remove(state);
        transitions.entrySet().removeIf(e -> {
            long key = e.getKey();
            int from = (int) (key >> 32);
            int to = (int) key;
            return from == state || to == state;
        });
    }

    public Set<Integer> getStates() {
        return states;
    }

    public Map<Long, String> getTransitions() {
        return transitions;
    }

    public int getStartState() {
        return startState;
    }

    public int getAcceptState() {
        return acceptState;
    }
}
