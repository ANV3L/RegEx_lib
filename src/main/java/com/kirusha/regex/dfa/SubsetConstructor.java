package com.kirusha.regex.dfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.kirusha.regex.nfa.NFA;
import com.kirusha.regex.nfa.NFAState;

public class SubsetConstructor {

    private int stateCounter;

    public DFA convert(NFA nfa) {
        stateCounter = 0;
        Set<NFAState> startEquiv = epsilonClosure(Set.of(nfa.getStartState()));
        
        Map<Set<NFAState>, DFAState> dfaStatesMap = new HashMap<>();
        DFAState startDFAState = new DFAState(stateCounter++, isAccepting(startEquiv, nfa.getAcceptState()));
        dfaStatesMap.put(startEquiv, startDFAState);
        
        Queue<Set<NFAState>> worklist = new LinkedList<>();
        worklist.add(startEquiv);
        
        while (!worklist.isEmpty()) {
            Set<NFAState> currentNFAStates = worklist.poll();
            DFAState currentDFAState = dfaStatesMap.get(currentNFAStates);
            
            for (String symbol : nfa.getAlphabet()) {
                Set<NFAState> nextNFAStates = epsilonClosure(move(currentNFAStates, symbol));
                if (nextNFAStates.isEmpty()) {
                    continue;
                }
                
                DFAState nextDFAState = dfaStatesMap.get(nextNFAStates);
                if (nextDFAState == null) {
                    nextDFAState = new DFAState(stateCounter++, isAccepting(nextNFAStates, nfa.getAcceptState()));
                    dfaStatesMap.put(nextNFAStates, nextDFAState);
                    worklist.add(nextNFAStates);
                }
                
                currentDFAState.addTransition(symbol, nextDFAState);
            }
        }
        
        Set<DFAState> acceptStates = new HashSet<>();
        Set<DFAState> allStates = new HashSet<>();
        for (DFAState state : dfaStatesMap.values()) {
            allStates.add(state);
            if (state.isAccepting()) {
                acceptStates.add(state);
            }
        }
        
        return new DFA(startDFAState, acceptStates, allStates, new HashSet<>(nfa.getAlphabet()));
    }

    private boolean isAccepting(Set<NFAState> states, NFAState acceptState) {
        return states.contains(acceptState);
    }

    private Set<NFAState> epsilonClosure(Set<NFAState> states) {
        Set<NFAState> closure = new HashSet<>(states);
        Queue<NFAState> worklist = new LinkedList<>(states);
        
        while (!worklist.isEmpty()) {
            NFAState state = worklist.poll();
            for (NFAState epsTarget : state.getEpsilonTransitions()) {
                if (closure.add(epsTarget)) {
                    worklist.add(epsTarget);
                }
            }
        }
        return closure;
    }

    private Set<NFAState> move(Set<NFAState> states, String symbol) {
        Set<NFAState> result = new HashSet<>();
        for (NFAState state : states) {
            Set<NFAState> targets = state.getTransitions().get(symbol);
            if (targets != null) {
                result.addAll(targets);
            }
        }
        return result;
    }
}
