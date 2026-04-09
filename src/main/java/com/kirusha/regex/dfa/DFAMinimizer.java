package com.kirusha.regex.dfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DFAMinimizer {

    public DFA minimize(DFA dfa) {
        DFA reachableDFA = removeUnreachable(dfa);
        
        Set<DFAState> acceptStates = reachableDFA.getAcceptStates();
        Set<DFAState> nonAcceptStates = new HashSet<>(reachableDFA.getStates());
        nonAcceptStates.removeAll(acceptStates);

        Set<Set<DFAState>> P = new HashSet<>();
        if (!acceptStates.isEmpty()) P.add(new HashSet<>(acceptStates));
        if (!nonAcceptStates.isEmpty()) P.add(new HashSet<>(nonAcceptStates));

        Queue<Set<DFAState>> W = new LinkedList<>();
        if (!acceptStates.isEmpty()) W.add(new HashSet<>(acceptStates));
        if (!nonAcceptStates.isEmpty()) W.add(new HashSet<>(nonAcceptStates));

        Set<String> alphabet = reachableDFA.getAlphabet();

        while (!W.isEmpty()) {
            Set<DFAState> A = W.poll();
            for (String c : alphabet) {
                Set<DFAState> X = new HashSet<>();
                for (DFAState state : reachableDFA.getStates()) {
                    DFAState target = state.getTransition(c);
                    if (target != null && A.contains(target)) {
                        X.add(state);
                    }
                }

                Set<Set<DFAState>> newP = new HashSet<>();
                for (Set<DFAState> Y : P) {
                    Set<DFAState> intersection = new HashSet<>(Y);
                    intersection.retainAll(X);
                    
                    Set<DFAState> difference = new HashSet<>(Y);
                    difference.removeAll(X);
                    
                    if (!intersection.isEmpty() && !difference.isEmpty()) {
                        newP.add(intersection);
                        newP.add(difference);
                        
                        if (W.contains(Y)) {
                            W.remove(Y);
                            W.add(intersection);
                            W.add(difference);
                        } else {
                            if (intersection.size() <= difference.size()) {
                                W.add(intersection);
                            } else {
                                W.add(difference);
                            }
                        }
                    } else {
                        newP.add(Y);
                    }
                }
                P = newP;
            }
        }

        Map<Set<DFAState>, DFAState> newStates = new HashMap<>();
        int idCounter = 0;
        for (Set<DFAState> partition : P) {
            boolean isAccept = false;
            for (DFAState s : partition) {
                if (s.isAccepting()) {
                    isAccept = true;
                    break;
                }
            }
            newStates.put(partition, new DFAState(idCounter++, isAccept));
        }

        DFAState newStart = null;
        for (Set<DFAState> partition : P) {
            if (partition.contains(reachableDFA.getStartState())) {
                newStart = newStates.get(partition);
                break;
            }
        }

        for (Set<DFAState> partition : P) {
            DFAState representative = partition.iterator().next();
            DFAState newState = newStates.get(partition);
            for (String c : alphabet) {
                DFAState oldTarget = representative.getTransition(c);
                if (oldTarget != null) {
                    for (Set<DFAState> targetPartition : P) {
                        if (targetPartition.contains(oldTarget)) {
                            newState.addTransition(c, newStates.get(targetPartition));
                            break;
                        }
                    }
                }
            }
        }

        Set<DFAState> finalAccepts = new HashSet<>();
        Set<DFAState> finalAll = new HashSet<>();
        for (DFAState s : newStates.values()) {
            finalAll.add(s);
            if (s.isAccepting()) {
                finalAccepts.add(s);
            }
        }

        return new DFA(newStart, finalAccepts, finalAll, new HashSet<>(alphabet));
    }

    public DFA removeUnreachable(DFA dfa) {
        Set<DFAState> reachable = new HashSet<>();
        Queue<DFAState> queue = new LinkedList<>();
        
        queue.add(dfa.getStartState());
        reachable.add(dfa.getStartState());
        
        while (!queue.isEmpty()) {
            DFAState current = queue.poll();
            for (DFAState target : current.getTransitions().values()) {
                if (target != null && reachable.add(target)) {
                    queue.add(target);
                }
            }
        }
        
        Set<DFAState> newAccept = new HashSet<>();
        for (DFAState s : dfa.getAcceptStates()) {
            if (reachable.contains(s)) {
                newAccept.add(s);
            }
        }
        
        return new DFA(dfa.getStartState(), newAccept, reachable, new HashSet<>(dfa.getAlphabet()));
    }
}
