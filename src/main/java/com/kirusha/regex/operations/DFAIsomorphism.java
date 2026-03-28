package com.kirusha.regex.operations;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAState;

import java.util.*;

/**
 * Проверка двух DFA на изоморфизм.
 */
public class DFAIsomorphism {

    /**
     * Проверяет, изоморфны ли два DFA.
     */
    public boolean areIsomorphic(DFA a, DFA b) {
        if (a.getStates().size() != b.getStates().size() || 
            a.getAcceptStates().size() != b.getAcceptStates().size() ||
            !a.getAlphabet().equals(b.getAlphabet())) {
            return false;
        }

        Map<DFAState, DFAState> mapping = new HashMap<>();
        Queue<DFAState> queue = new LinkedList<>();

        mapping.put(a.getStartState(), b.getStartState());
        queue.add(a.getStartState());

        while (!queue.isEmpty()) {
            DFAState currentA = queue.poll();
            DFAState currentB = mapping.get(currentA);

            if (currentA.isAccepting() != currentB.isAccepting()) {
                return false;
            }

            for (String symbol : a.getAlphabet()) {
                DFAState targetA = currentA.getTransition(symbol);
                DFAState targetB = currentB.getTransition(symbol);

                if (targetA == null && targetB == null) continue;
                if (targetA == null || targetB == null) return false;

                if (mapping.containsKey(targetA)) {
                    if (!mapping.get(targetA).equals(targetB)) {
                        return false;
                    }
                } else {
                    mapping.put(targetA, targetB);
                    queue.add(targetA);
                }
            }
        }

        return true;
    }

    /**
     * Проверяет, эквивалентны ли два DFA.
     */
    public boolean areEquivalent(DFA a, DFA b) {
        DFAOperations ops = new DFAOperations();
        DFA diff1 = ops.difference(a, b);
        DFA diff2 = ops.difference(b, a);
        return diff1.getAcceptStates().isEmpty() && diff2.getAcceptStates().isEmpty();
    }
}
