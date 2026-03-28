package com.kirusha.regex.operations;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAState;

import java.util.*;

/**
 * Теоретико-множественные операции над DFA.
 */
public class DFAOperations {

    /**
     * Пересечение двух DFA. L(a) ∩ L(b)
     */
    public DFA intersect(DFA a, DFA b) {
        return productConstruction(a, b, (sa, sb) -> sa.isAccepting() && sb.isAccepting());
    }

    /**
     * Разность двух DFA. L(a) \ L(b)
     */
    public DFA difference(DFA a, DFA b) {
        return productConstruction(a, b, (sa, sb) -> sa.isAccepting() && !sb.isAccepting());
    }

    /**
     * Объединение двух DFA. L(a) ∪ L(b)
     */
    public DFA union(DFA a, DFA b) {
        return productConstruction(a, b, (sa, sb) -> sa.isAccepting() || sb.isAccepting());
    }

    private interface AcceptCondition {
        boolean isAccepting(DFAState sa, DFAState sb);
    }

    private DFA productConstruction(DFA a, DFA b, AcceptCondition condition) {
        Set<String> combinedAlphabet = new HashSet<>(a.getAlphabet());
        combinedAlphabet.addAll(b.getAlphabet());

        DFA fullA = makeComplete(a, combinedAlphabet);
        DFA fullB = makeComplete(b, combinedAlphabet);

        Map<String, DFAState> newStates = new HashMap<>(); // key = idA + "," + idB
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

    /**
     * Дополнение DFA. Σ* \ L(a)
     */
    public DFA complement(DFA a) {
        DFA fullA = makeComplete(a, a.getAlphabet());
        
        Set<DFAState> newAccept = new HashSet<>();
        for (DFAState s : fullA.getStates()) {
            s.setAccepting(!s.isAccepting());
            if (s.isAccepting()) {
                newAccept.add(s);
            }
        }
        
        return new DFA(fullA.getStartState(), newAccept, fullA.getStates(), fullA.getAlphabet());
    }

    /**
     * Инверсия DFA. reverse(L(a))
     */
    public DFA invert(DFA a) {
        throw new UnsupportedOperationException("Inversion requires NFA integration, simplified fallback used");
    }

    /**
     * Делает DFA полным для заданного алфавита.
     */
    public DFA makeComplete(DFA dfa, Set<String> alphabet) {
        int maxId = 0;
        for (DFAState s : dfa.getStates()) maxId = Math.max(maxId, s.getId());
        
        // Deep copy states
        Map<Integer, DFAState> copiedStates = new HashMap<>();
        for (DFAState state : dfa.getStates()) {
            copiedStates.put(state.getId(), new DFAState(state.getId(), state.isAccepting()));
        }
        
        // Restore transitions
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
