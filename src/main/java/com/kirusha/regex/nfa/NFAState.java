package com.kirusha.regex.nfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class NFAState {

    private final int id;

    private final Map<String, Set<NFAState>> transitions;

    private final Set<NFAState> epsilonTransitions;

    private int groupOpen = -1;

    private int groupClose = -1;

    private final Map<Integer, NFAState> backrefTransitions;

    public NFAState(int id) {
        this.id = id;
        this.transitions = new HashMap<>();
        this.epsilonTransitions = new HashSet<>();
        this.backrefTransitions = new HashMap<>();
    }

    public void addTransition(String symbol, NFAState target) {
        transitions.computeIfAbsent(symbol, k -> new HashSet<>()).add(target);
    }

    public void addEpsilonTransition(NFAState target) {
        epsilonTransitions.add(target);
    }
    
    public void addBackrefTransition(int group, NFAState target) {
        backrefTransitions.put(group, target);
    }

    public int getGroupOpen() {
        return groupOpen;
    }

    public void setGroupOpen(int groupOpen) {
        this.groupOpen = groupOpen;
    }

    public int getGroupClose() {
        return groupClose;
    }

    public void setGroupClose(int groupClose) {
        this.groupClose = groupClose;
    }

    public Map<Integer, NFAState> getBackrefTransitions() {
        return backrefTransitions;
    }

    public int getId() {
        return id;
    }

    public Map<String, Set<NFAState>> getTransitions() {
        return transitions;
    }

    public Set<NFAState> getEpsilonTransitions() {
        return epsilonTransitions;
    }

    @Override
    public String toString() {
        return "State(" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NFAState nfaState = (NFAState) o;
        return id == nfaState.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
