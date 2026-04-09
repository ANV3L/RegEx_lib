package com.kirusha.regex.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kirusha.regex.nfa.NFA;
import com.kirusha.regex.nfa.NFAState;

public class NFAEngine {

    public boolean matches(NFA nfa, String input) {
        return match(nfa, input, nfa.getGroupCount()).matches();
    }

    public MatchResult match(NFA nfa, String input, int groupCount) {
        String[] bestMatchGroups = new String[groupCount + 1];
        int[] groupStarts = new int[groupCount + 1];
        Arrays.fill(groupStarts, -1);
        boolean matched = dfs(nfa, input, 0, nfa.getStartState(), new HashSet<>(), bestMatchGroups, groupStarts, "");
        
        if (matched) {
            bestMatchGroups[0] = input;
            List<String> groupsList = new ArrayList<>();
            for (String g : bestMatchGroups) {
                groupsList.add(g == null ? "" : g);
            }
            return new MatchResult(true, groupsList);
        }
        
        return MatchResult.noMatch();
    }

    private boolean dfs(NFA nfa, String input, int pos, NFAState current,
                         Set<NFAState> visitedEps, String[] groups,
                         int[] groupStarts, String pathStr) {

        int groupOpen = current.getGroupOpen();
        int savedGroupStart = -1;
        if (groupOpen != -1) {
            savedGroupStart = groupStarts[groupOpen];
            groupStarts[groupOpen] = pos;
        }

        int groupClose = current.getGroupClose();
        String savedGroupValue = null;
        if (groupClose != -1 && groupStarts[groupClose] != -1) {
            savedGroupValue = groups[groupClose];
            groups[groupClose] = input.substring(groupStarts[groupClose], pos);
        }

        if (pos == input.length() && current.equals(nfa.getAcceptState())) {
            return true;
        }

        for (NFAState epsTarget : current.getEpsilonTransitions()) {
            if (!visitedEps.contains(epsTarget)) {
                visitedEps.add(epsTarget);
                if (dfs(nfa, input, pos, epsTarget, visitedEps, groups, groupStarts, pathStr)) {
                    return true;
                }
                visitedEps.remove(epsTarget);
            }
        }

        if (pos < input.length()) {
            String symbol = String.valueOf(input.charAt(pos));
            Set<NFAState> targets = current.getTransitions().get(symbol);
            if (targets != null) {
                for (NFAState target : targets) {
                    int[] gsClone = groupStarts.clone();
                    String[] grClone = groups.clone();
                    if (dfs(nfa, input, pos + 1, target, new HashSet<>(), grClone, gsClone, pathStr + symbol)) {
                        System.arraycopy(grClone, 0, groups, 0, groups.length);
                        System.arraycopy(gsClone, 0, groupStarts, 0, groupStarts.length);
                        return true;
                    }
                }
            }
        }

        for (Map.Entry<Integer, NFAState> entry : current.getBackrefTransitions().entrySet()) {
            int groupNum = entry.getKey();
            NFAState target = entry.getValue();
            String expectedStr = groups[groupNum];
            if (expectedStr != null && pos + expectedStr.length() <= input.length()) {
                if (input.startsWith(expectedStr, pos)) {
                    int[] gsClone = groupStarts.clone();
                    String[] grClone = groups.clone();
                    if (dfs(nfa, input, pos + expectedStr.length(), target, new HashSet<>(),
                            grClone, gsClone, pathStr + expectedStr)) {
                        System.arraycopy(grClone, 0, groups, 0, groups.length);
                        System.arraycopy(gsClone, 0, groupStarts, 0, groupStarts.length);
                        return true;
                    }
                }
            }
        }

        if (groupClose != -1 && groupStarts[groupClose] != -1) {
            groups[groupClose] = savedGroupValue;
        }
        if (groupOpen != -1) {
            groupStarts[groupOpen] = savedGroupStart;
        }

        return false;
    }
}
