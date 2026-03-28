package com.kirusha.regex.recovery;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAState;

import java.util.*;

/**
 * Восстановление регулярного выражения из DFA методом исключения состояний.
 */
public class StateEliminator {

    /**
     * Восстанавливает регулярное выражение из DFA.
     */
    public String recover(DFA dfa) {
        GNFA gnfa = dfaToGnfa(dfa);
        
        List<Integer> statesToRemove = new ArrayList<>(gnfa.getStates());
        statesToRemove.remove((Integer) gnfa.getStartState());
        statesToRemove.remove((Integer) gnfa.getAcceptState());
        
        for (int state : statesToRemove) {
            eliminateState(gnfa, state);
        }
        
        String regex = gnfa.getTransition(gnfa.getStartState(), gnfa.getAcceptState());
        return regex == null ? "∅" : simplify(regex);
    }

    /**
     * Преобразует DFA в GNFA.
     */
    private GNFA dfaToGnfa(DFA dfa) {
        int maxId = 0;
        for (DFAState s : dfa.getStates()) {
            maxId = Math.max(maxId, s.getId());
        }
        
        int startGnfa = maxId + 1;
        int acceptGnfa = maxId + 2;
        
        Set<Integer> states = new HashSet<>();
        states.add(startGnfa);
        states.add(acceptGnfa);
        
        Map<Long, String> transitions = new HashMap<>();
        
        // Epsilon start
        transitions.put(GNFA.encodePair(startGnfa, dfa.getStartState().getId()), "#");
        
        for (DFAState s : dfa.getStates()) {
            states.add(s.getId());
            if (s.isAccepting()) {
                transitions.put(GNFA.encodePair(s.getId(), acceptGnfa), "#");
            }
            
            // Объединяем символы для переходов между одними и теми же состояниями
            Map<Integer, List<String>> targets = new HashMap<>();
            for (String symbol : dfa.getAlphabet()) {
                DFAState target = s.getTransition(symbol);
                if (target != null) {
                    targets.computeIfAbsent(target.getId(), k -> new ArrayList<>()).add(symbol);
                }
            }
            
            for (Map.Entry<Integer, List<String>> entry : targets.entrySet()) {
                List<String> syms = entry.getValue();
                String regex = String.join("|", syms);
                if (syms.size() > 1) regex = "[" + regex.replace("|", "") + "]"; // упрощение для char class
                transitions.put(GNFA.encodePair(s.getId(), entry.getKey()), regex);
            }
        }
        
        return new GNFA(states, transitions, startGnfa, acceptGnfa);
    }

    /**
     * Исключает одно состояние из GNFA.
     */
    private void eliminateState(GNFA gnfa, int state) {
        List<Integer> incoming = new ArrayList<>();
        List<Integer> outgoing = new ArrayList<>();
        
        for (int s : gnfa.getStates()) {
            if (s == state) continue;
            if (gnfa.getTransition(s, state) != null) incoming.add(s);
            if (gnfa.getTransition(state, s) != null) outgoing.add(s);
        }
        
        String loop = gnfa.getTransition(state, state);
        String loopStr = "";
        if (loop != null) {
            loopStr = (loop.length() > 1 && !loop.startsWith("(")) ? "(" + loop + ")*" : loop + "*";
        }
        
        for (int in : incoming) {
            String r_in = gnfa.getTransition(in, state);
            for (int out : outgoing) {
                String r_out = gnfa.getTransition(state, out);
                
                String newPath = r_in + loopStr + r_out;
                newPath = newPath.replace("#", ""); // простое упрощение epsilon
                if (newPath.isEmpty()) newPath = "#";
                
                String current = gnfa.getTransition(in, out);
                if (current != null) {
                    gnfa.setTransition(in, out, current + "|" + newPath);
                } else {
                    gnfa.setTransition(in, out, newPath);
                }
            }
        }
        
        gnfa.removeState(state);
    }

    private String simplify(String regex) {
        // Простые эвристики
        String r = regex.replace("(#)*", "#").replace("()", "#");
        if (r.startsWith("(|")) r = "#" + r.substring(1);
        return r;
    }
}
