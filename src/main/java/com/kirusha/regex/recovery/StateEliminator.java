package com.kirusha.regex.recovery;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAState;

import java.util.*;

public class StateEliminator {

    public String recover(DFA dfa) {
        if (dfa.getAcceptStates().isEmpty()) {
            return "∅";
        }

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

        transitions.put(GNFA.encodePair(startGnfa, dfa.getStartState().getId()), "#");

        for (DFAState s : dfa.getStates()) {
            states.add(s.getId());
            if (s.isAccepting()) {
                String existing = transitions.get(GNFA.encodePair(s.getId(), acceptGnfa));
                if (existing == null) {
                    transitions.put(GNFA.encodePair(s.getId(), acceptGnfa), "#");
                }
            }

            Map<Integer, List<String>> targets = new HashMap<>();
            for (String symbol : dfa.getAlphabet()) {
                DFAState target = s.getTransition(symbol);
                if (target != null) {
                    targets.computeIfAbsent(target.getId(), k -> new ArrayList<>()).add(symbol);
                }
            }

            for (Map.Entry<Integer, List<String>> entry : targets.entrySet()) {
                List<String> syms = entry.getValue();
                String regex;
                if (syms.size() == 1) {
                    regex = escapeSymbol(syms.get(0));
                } else {
                    StringBuilder sb = new StringBuilder("[");
                    for (String sym : syms) {
                        sb.append(escapeSymbol(sym));
                    }
                    sb.append("]");
                    regex = sb.toString();
                }

                long key = GNFA.encodePair(s.getId(), entry.getKey());
                String existing = transitions.get(key);
                if (existing != null) {
                    transitions.put(key, altRegex(existing, regex));
                } else {
                    transitions.put(key, regex);
                }
            }
        }

        return new GNFA(states, transitions, startGnfa, acceptGnfa);
    }

    private String escapeSymbol(String sym) {
        // Escape meta characters that would be misinterpreted by our parser
        if (sym.length() == 1) {
            char c = sym.charAt(0);
            if (c == '|' || c == '*' || c == '(' || c == ')' ||
                    c == '[' || c == ']' || c == '{' || c == '}' || c == '#' || c == '\\') {
                return "\\" + c;
            }
        }
        return sym;
    }

    private void eliminateState(GNFA gnfa, int state) {
        List<Integer> statesList = new ArrayList<>(gnfa.getStates());

        String loop = gnfa.getTransition(state, state);

        for (int qi : statesList) {
            if (qi == state)
                continue;
            String r_in = gnfa.getTransition(qi, state);
            if (r_in == null)
                continue;

            for (int qj : statesList) {
                if (qj == state)
                    continue;
                String r_out = gnfa.getTransition(state, qj);
                if (r_out == null)
                    continue;

                String middle;
                if (loop != null) {
                    middle = concatRegex(concatRegex(r_in, wrapStar(loop)), r_out);
                } else {
                    middle = concatRegex(r_in, r_out);
                }

                String existing = gnfa.getTransition(qi, qj);
                if (existing != null) {
                    gnfa.setTransition(qi, qj, altRegex(existing, middle));
                } else {
                    gnfa.setTransition(qi, qj, middle);
                }
            }
        }

        gnfa.removeState(state);
    }

    private String concatRegex(String a, String b) {
        if (a == null || a.equals("#"))
            return b;
        if (b == null || b.equals("#"))
            return a;
        // Wrap alternation in parens for correct precedence
        String left = needsParensForConcat(a) ? "(" + a + ")" : a;
        String right = needsParensForConcat(b) ? "(" + b + ")" : b;
        return left + right;
    }

    private boolean needsParensForConcat(String r) {
        int depth = 0;
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            if (c == '\\') {
                i++;
                continue;
            }
            if (c == '(')
                depth++;
            else if (c == ')')
                depth--;
            else if (c == '[') {
                // skip to matching ]
                i++;
                while (i < r.length() && r.charAt(i) != ']') {
                    if (r.charAt(i) == '\\')
                        i++;
                    i++;
                }
            } else if (c == '|' && depth == 0)
                return true;
        }
        return false;
    }

    private String altRegex(String a, String b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return a + "|" + b;
    }

    private String wrapStar(String r) {
        if (r.equals("#"))
            return "#";
        if (r.length() == 1)
            return r + "*";
        if (r.length() == 2 && r.charAt(0) == '\\')
            return r + "*"; // escaped char
        if (r.startsWith("(") && findMatchingParen(r, 0) == r.length() - 1) {
            return r + "*";
        }
        if (r.startsWith("[") && findMatchingClose(r, '[', ']') == r.length() - 1) {
            return r + "*";
        }
        return "(" + r + ")*";
    }

    private int findMatchingParen(String s, int open) {
        int depth = 0;
        for (int i = open; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                continue;
            }
            if (c == '(')
                depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0)
                    return i;
            }
        }
        return -1;
    }

    private int findMatchingClose(String s, char open, char close) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                continue;
            }
            if (c == open)
                depth++;
            else if (c == close) {
                depth--;
                if (depth == 0)
                    return i;
            }
        }
        return -1;
    }

    private String simplify(String regex) {
        if (regex == null || regex.isEmpty())
            return "#";
        return regex;
    }
}
