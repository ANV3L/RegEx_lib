package com.kirusha.regex.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAMinimizer;
import com.kirusha.regex.dfa.DFAState;

public class DFAOperations {

    private final DFAMinimizer minimizer = new DFAMinimizer();

    public DFA intersect(DFA a, DFA b) {
        DFA product = productConstruction(a, b, (sa, sb) -> sa.isAccepting() && sb.isAccepting());
        return minimizer.minimize(product);
    }

    public DFA difference(DFA a, DFA b) {
        DFA product = productConstruction(a, b, (sa, sb) -> sa.isAccepting() && !sb.isAccepting());
        return minimizer.minimize(product);
    }

    public DFA union(DFA a, DFA b) {
        DFA product = productConstruction(a, b, (sa, sb) -> sa.isAccepting() || sb.isAccepting());
        return minimizer.minimize(product);
    }

    private interface AcceptCondition {
        boolean isAccepting(DFAState sa, DFAState sb);
    }

    private DFA productConstruction(DFA a, DFA b, AcceptCondition condition) {
        Set<String> combinedAlphabet = new HashSet<>(a.getAlphabet());
        combinedAlphabet.addAll(b.getAlphabet());

        DFA fullA = makeComplete(a, combinedAlphabet);
        DFA fullB = makeComplete(b, combinedAlphabet);

        Map<String, DFAState> newStates = new HashMap<>();
        int idCounter = 0;

        String startKey = fullA.getStartState().getId() + "," + fullB.getStartState().getId();
        DFAState newStart = new DFAState(idCounter++, condition.isAccepting(fullA.getStartState(), fullB.getStartState()));
        newStates.put(startKey, newStart);

        Queue<DFAStatePair> queue = new LinkedList<>();
        queue.add(new DFAStatePair(fullA.getStartState(), fullB.getStartState(), newStart));

        while (!queue.isEmpty()) {
            DFAStatePair current = queue.poll();
            
            for (String symbol : combinedAlphabet) {
                DFAState targetA = current.sa.getTransition(symbol);
                DFAState targetB = current.sb.getTransition(symbol);

                String key = targetA.getId() + "," + targetB.getId();
                DFAState newTarget = newStates.get(key);

                if (newTarget == null) {
                    newTarget = new DFAState(idCounter++, condition.isAccepting(targetA, targetB));
                    newStates.put(key, newTarget);
                    queue.add(new DFAStatePair(targetA, targetB, newTarget));
                }

                current.newS.addTransition(symbol, newTarget);
            }
        }

        Set<DFAState> acceptStates = new HashSet<>();
        Set<DFAState> allStates = new HashSet<>(newStates.values());
        for (DFAState s : allStates) {
            if (s.isAccepting()) acceptStates.add(s);
        }

        return new DFA(newStart, acceptStates, allStates, combinedAlphabet);
    }

    private static class DFAStatePair {
        DFAState sa;
        DFAState sb;
        DFAState newS;
        DFAStatePair(DFAState sa, DFAState sb, DFAState newS) {
            this.sa = sa; this.sb = sb; this.newS = newS;
        }
    }

    public DFA complement(DFA a) {
        DFA fullA = makeComplete(a, a.getAlphabet());
        
        Map<Integer, DFAState> copiedStates = new HashMap<>();
        for (DFAState state : fullA.getStates()) {
            copiedStates.put(state.getId(), new DFAState(state.getId(), !state.isAccepting()));
        }
        
        for (DFAState oldState : fullA.getStates()) {
            DFAState newState = copiedStates.get(oldState.getId());
            for (String symbol : fullA.getAlphabet()) {
                DFAState oldTarget = oldState.getTransition(symbol);
                if (oldTarget != null) {
                    newState.addTransition(symbol, copiedStates.get(oldTarget.getId()));
                }
            }
        }
        
        DFAState newStartState = copiedStates.get(fullA.getStartState().getId());
        Set<DFAState> newAcceptStates = new HashSet<>();
        Set<DFAState> allNewStates = new HashSet<>(copiedStates.values());
        
        for (DFAState s : allNewStates) {
            if (s.isAccepting()) {
                newAcceptStates.add(s);
            }
        }
        
        DFA resultDfa = new DFA(newStartState, newAcceptStates, allNewStates, fullA.getAlphabet());
        return minimizer.minimize(resultDfa);
    }

    public DFA makeComplete(DFA dfa, Set<String> alphabet) {
        int maxId = 0;
        for (DFAState s : dfa.getStates()) maxId = Math.max(maxId, s.getId());
        
        Map<Integer, DFAState> copiedStates = new HashMap<>();
        for (DFAState state : dfa.getStates()) {
            copiedStates.put(state.getId(), new DFAState(state.getId(), state.isAccepting()));
        }
        
        for (DFAState oldState : dfa.getStates()) {
            DFAState newState = copiedStates.get(oldState.getId());
            for (String symbol : dfa.getAlphabet()) {
                DFAState oldTarget = oldState.getTransition(symbol);
                if (oldTarget != null) {
                    newState.addTransition(symbol, copiedStates.get(oldTarget.getId()));
                }
            }
        }
        
        DFAState newStartState = copiedStates.get(dfa.getStartState().getId());
        Set<DFAState> newAcceptStates = new HashSet<>();
        for (DFAState oldAccept : dfa.getAcceptStates()) {
            newAcceptStates.add(copiedStates.get(oldAccept.getId()));
        }

        DFAState deadState = null;
        Set<DFAState> allStates = new HashSet<>(copiedStates.values());

        for (DFAState state : copiedStates.values()) {
            for (String symbol : alphabet) {
                if (state.getTransition(symbol) == null) {
                    if (deadState == null) {
                        deadState = new DFAState(maxId + 1, false);
                        allStates.add(deadState);
                        for (String sym : alphabet) {
                            deadState.addTransition(sym, deadState);
                        }
                    }
                    state.addTransition(symbol, deadState);
                }
            }
        }

        return new DFA(newStartState, newAcceptStates, allStates, alphabet);
    }
}