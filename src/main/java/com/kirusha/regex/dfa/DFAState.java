package com.kirusha.regex.dfa;

import java.util.Map;
import java.util.Objects;

public class DFAState {

    private final int id;

    private final Map<String, DFAState> transitions;

    private boolean accepting;

    public DFAState(int id, boolean accepting) {
        this.id = id;
        this.accepting = accepting;
        this.transitions = new java.util.HashMap<>();
    }

    public void addTransition(String symbol, DFAState target) {
        transitions.put(symbol, target);
    }

    public DFAState getTransition(String symbol) {
        return transitions.get(symbol);
    }

    public int getId() {
        return id;
    }

    public Map<String, DFAState> getTransitions() {
        return transitions;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    @Override
    public String toString() {
        return "DFAState(" + id + (accepting ? ", accept" : "") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFAState dfaState = (DFAState) o;
        return id == dfaState.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
