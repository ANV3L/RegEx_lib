package com.kirusha.regex.nfa;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import com.kirusha.regex.parser.ParserResult;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.AlternationNode;
import com.kirusha.regex.parser.ast.BackReferenceNode;
import com.kirusha.regex.parser.ast.CharClassNode;
import com.kirusha.regex.parser.ast.ConcatenationNode;
import com.kirusha.regex.parser.ast.EpsilonNode;
import com.kirusha.regex.parser.ast.GroupNode;
import com.kirusha.regex.parser.ast.KleeneStarNode;
import com.kirusha.regex.parser.ast.LiteralNode;
import com.kirusha.regex.parser.ast.PalindromizationNode;
import com.kirusha.regex.parser.ast.RepeatNode;
import com.kirusha.regex.parser.ast.XORNode;

public class ThompsonBuilder {

    private int stateCounter;

    private final Map<Class<? extends ASTNode>, Function<ASTNode, NFAFragment>> builders;

    public ThompsonBuilder() {
        this.builders = new HashMap<>();

        register(LiteralNode.class, this::buildLiteral);
        register(EpsilonNode.class, this::buildEpsilon);
        register(AlternationNode.class, this::buildAlternation);
        register(ConcatenationNode.class, this::buildConcatenation);
        register(KleeneStarNode.class, this::buildKleeneStar);
        register(RepeatNode.class, this::buildRepeat);
        register(CharClassNode.class, this::buildCharClass);
        register(GroupNode.class, this::buildGroup);
        register(BackReferenceNode.class, this::buildBackReference);
        register(XORNode.class, this::buildXOR);

    }

    @SuppressWarnings("unchecked")
    private <T extends ASTNode> void register(
            Class<T> nodeClass,
            Function<T, NFAFragment> builder) {
        builders.put(nodeClass, (Function<ASTNode, NFAFragment>) builder);
    }

    public NFA build(ParserResult pr) {
        if (pr == null) {
            throw new IllegalArgumentException("ParserResult is null");
        }

        stateCounter = 0;

        ASTNode root = pr.getRoot();
        NFAFragment fragment = buildFragment(root);

        Set<NFAState> states = collectStates(fragment.getStart());
        Set<String> alphabet = collectAlphabet(states);

        return new NFA(
                fragment.getStart(),
                fragment.getAccept(),
                states,
                alphabet,
                pr.getGroupCount());

    }

    private NFAFragment buildFragment(ASTNode node) {
        Function<ASTNode, NFAFragment> builder = builders.get(node.getClass());

        if (builder == null) {
            throw new IllegalArgumentException(
                    "No builder registered for: " + node.getClass().getSimpleName());
        }

        return builder.apply(node);
    }

    // ===================

    private NFAFragment buildLiteral(LiteralNode node) {
        NFAState start = newState();
        NFAState accept = newState();
        start.addTransition(node.getValue(), accept);
        return new NFAFragment(start, accept);
    }

    private NFAFragment buildEpsilon(EpsilonNode node) {
        NFAState start = newState();
        NFAState accept = newState();
        start.addEpsilonTransition(accept);
        return new NFAFragment(start, accept);
    }

    private NFAFragment buildAlternation(AlternationNode node) {
        NFAFragment left = buildFragment(node.getLeft());
        NFAFragment right = buildFragment(node.getRight());

        NFAState newStart = newState();
        NFAState newAccept = newState();

        newStart.addEpsilonTransition(left.getStart());
        newStart.addEpsilonTransition(right.getStart());

        left.getAccept().addEpsilonTransition(newAccept);
        right.getAccept().addEpsilonTransition(newAccept);

        return new NFAFragment(newStart, newAccept);
    }

    private NFAFragment buildXOR(XORNode node) {
        NFAFragment left = buildFragment(node.getLeft());
        NFAFragment right = buildFragment(node.getRight());

        Set<String> alphabet = new HashSet<>();
        alphabet.addAll(collectAlphabetFromFragment(left));
        alphabet.addAll(collectAlphabetFromFragment(right));

        NFAState leftAccept = left.getAccept();
        NFAState rightaccept = right.getAccept();

        Set<NFAState> startClosureL = getEpsilonClosureSet(Collections.singleton(left.getStart()));
        Set<NFAState> startClosureR = getEpsilonClosureSet(Collections.singleton(right.getStart()));

        Map<Pair<Set<NFAState>, Set<NFAState>>, NFAState> stateMap = new HashMap(); 
        Queue<Pair<Set<NFAState>, Set<NFAState>>> queue = new ArrayDeque<>();

        Pair<Set<NFAState>, Set<NFAState>> startPair = new Pair<>(startClosureL, startClosureR);

        NFAState newStart = newState();
        stateMap.put(startPair, newStart); 
        queue.add(startPair);

        NFAState newAccept = newState();

        while(!queue.isEmpty()){
            Pair<Set<NFAState>, Set<NFAState>> current = queue.poll();
            NFAState curState = stateMap.get(current);

            boolean lAccept = current.first.contains(leftAccept);
            boolean rAccept = current.second.contains(rightaccept);

            if (lAccept ^ rAccept){
                curState.addEpsilonTransition(newAccept);
            }

            for (String s : alphabet) {
                Set<NFAState> nextL = moveAndClose(current.first, s);
                Set<NFAState> nextR = moveAndClose(current.second, s);

                if (nextL.isEmpty() && nextR.isEmpty()){
                    continue;
                } 

                Pair<Set<NFAState>, Set<NFAState>> nextPair = new Pair<>(nextL, nextR);
                NFAState nextState = stateMap.get(nextPair);

                if (nextState == null){
                    nextState = newState();
                    stateMap.put(nextPair, nextState);
                    queue.add(nextPair);
                }

                curState.addTransition(s, nextState);

            }

        }







        return new NFAFragment(newStart, newAccept);
    }

    private NFAFragment buildConcatenation(ConcatenationNode node) {
        NFAFragment left = buildFragment(node.getLeft());
        NFAFragment right = buildFragment(node.getRight());

        left.getAccept().addEpsilonTransition(right.getStart());

        return new NFAFragment(left.getStart(), right.getAccept());

    }

    private NFAFragment buildKleeneStar(KleeneStarNode node) {
        NFAFragment child = buildFragment(node.getChild());

        NFAState newStart = newState();
        NFAState newAccept = newState();

        newStart.addEpsilonTransition(child.getStart());
        newStart.addEpsilonTransition(newAccept);

        child.getAccept().addEpsilonTransition(child.getStart());
        child.getAccept().addEpsilonTransition(newAccept);

        return new NFAFragment(newStart, newAccept);

    }

    private NFAFragment buildRepeat(RepeatNode node) {
        int count = node.getCount();

        if (count == 0) {
            return buildEpsilon(new EpsilonNode());
        }

        NFAFragment result = buildFragment(node.getChild());

        for (int i = 1; i < count; i++) {
            NFAFragment next = buildFragment(node.getChild());
            result.getAccept().addEpsilonTransition(next.getStart());
            result = new NFAFragment(result.getStart(), next.getAccept());
        }

        return result;
    }

    private NFAFragment buildCharClass(CharClassNode node) {
        if (node.isEmpty())
            return buildEpsilon(new EpsilonNode());

        NFAState start = newState();
        NFAState accept = newState();

        for (String symbol : node.getSymbols()) {
            start.addTransition(symbol, accept);
        }

        return new NFAFragment(start, accept);
    }

    private NFAFragment buildGroup(GroupNode node) {
        NFAFragment child = buildFragment(node.getChild());

        NFAState start = newState();
        start.setGroupOpen(node.getGroupNumber());
        start.addEpsilonTransition(child.getStart());

        NFAState accept = newState();
        accept.setGroupClose(node.getGroupNumber());
        child.getAccept().addEpsilonTransition(accept);

        return new NFAFragment(start, accept);
    }

    private NFAFragment buildBackReference(BackReferenceNode node) {
        NFAState start = newState();
        NFAState accept = newState();
        start.addBackrefTransition(node.getGroupNumber(), accept);
        return new NFAFragment(start, accept);
    }

    // ===================

    private NFAState newState() {
        return new NFAState(stateCounter++);
    }

    private Set<NFAState> collectStates(NFAState start) {
        Set<NFAState> visited = new HashSet<>();
        Deque<NFAState> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            NFAState current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }

            for (NFAState target : current.getEpsilonTransitions()) {
                if (!visited.contains(target)) {
                    stack.push(target);
                }
            }

            for (Set<NFAState> targets : current.getTransitions().values()) {
                for (NFAState target : targets) {
                    if (!visited.contains(target)) {
                        stack.push(target);
                    }
                }
            }

            for (NFAState target : current.getBackrefTransitions().values()) {
                if (!visited.contains(target)) {
                    stack.push(target);
                }
            }
        }

        return visited;
    }

    private Set<String> collectAlphabet(Set<NFAState> states) {
        Set<String> alphabet = new HashSet<>();
        for (NFAState state : states) {
            alphabet.addAll(state.getTransitions().keySet());
        }
        return alphabet;
    }

    private Set<NFAState> getEpsilonClosureSet(Set<NFAState> states) {
        Set<NFAState> closure = new HashSet<>(states);
        Deque<NFAState> stack = new ArrayDeque<>(states);
        while (!stack.isEmpty()) {
            NFAState s = stack.pop();
            for (NFAState t : s.getEpsilonTransitions()) {
                if (closure.add(t))
                    stack.push(t);
            }
        }
        return closure;
    }

    private Set<NFAState> moveAndClose(Set<NFAState> states, String symbol) {
        Set<NFAState> moved = new HashSet<>();
        for (NFAState s : states) {
            Set<NFAState> targets = s.getTransitions().getOrDefault(symbol, Collections.emptySet());
            moved.addAll(targets);
        }
        return moved.isEmpty() ? Collections.emptySet() : getEpsilonClosureSet(moved);
    }

    private Set<NFAState> collectStatesFromFragment(NFAFragment fragment) {
        Set<NFAState> visited = new HashSet<>();
        Deque<NFAState> stack = new ArrayDeque<>();
        stack.push(fragment.getStart());

        while (!stack.isEmpty()) {
            NFAState current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }

            for (NFAState target : current.getEpsilonTransitions()) {
                if (!visited.contains(target)) {
                    stack.push(target);
                }
            }

            for (Set<NFAState> targets : current.getTransitions().values()) {
                for (NFAState target : targets) {
                    if (!visited.contains(target)) {
                        stack.push(target);
                    }
                }
            }

            for (NFAState target : current.getBackrefTransitions().values()) {
                if (!visited.contains(target)) {
                    stack.push(target);
                }
            }
        }

        return visited;
    }

    public Set<String> collectAlphabetFromFragment(NFAFragment fragment) {
        if (fragment == null) {
            return Collections.emptySet();
        }

        Set<NFAState> allStates = collectStatesFromFragment(fragment);
        return collectAlphabet(allStates);
    }

    public Set<String> collectAlphabetFromAST(ASTNode node) {
        if (node == null) {
            return Collections.emptySet();
        }

        Set<String> alphabet = new HashSet<>();
        collectAlphabetFromASTRecursive(node, alphabet);
        return alphabet;
    }

    private void collectAlphabetFromASTRecursive(ASTNode node, Set<String> alphabet) {
        if (node == null) {
            return;
        }

        if (node instanceof LiteralNode) {
            alphabet.add(((LiteralNode) node).getValue());
            return;
        }

        if (node instanceof CharClassNode) {
            alphabet.addAll(((CharClassNode) node).getSymbols());
            return;
        }

        if (node instanceof BackReferenceNode || node instanceof EpsilonNode) {
            return;
        }

        if (node instanceof ConcatenationNode) {
            ConcatenationNode concat = (ConcatenationNode) node;
            collectAlphabetFromASTRecursive(concat.getLeft(), alphabet);
            collectAlphabetFromASTRecursive(concat.getRight(), alphabet);
            return;
        }

        if (node instanceof AlternationNode) {
            AlternationNode alt = (AlternationNode) node;
            collectAlphabetFromASTRecursive(alt.getLeft(), alphabet);
            collectAlphabetFromASTRecursive(alt.getRight(), alphabet);
            return;
        }

        if (node instanceof KleeneStarNode) {
            collectAlphabetFromASTRecursive(((KleeneStarNode) node).getChild(), alphabet);
            return;
        }

        if (node instanceof RepeatNode) {
            collectAlphabetFromASTRecursive(((RepeatNode) node).getChild(), alphabet);
            return;
        }

        if (node instanceof GroupNode) {
            collectAlphabetFromASTRecursive(((GroupNode) node).getChild(), alphabet);
            return;
        }

        if (node instanceof PalindromizationNode) {
            collectAlphabetFromASTRecursive(((PalindromizationNode) node).getChild(), alphabet);
        }
    }

    private static class Pair<T, U> {
        final T first;
        final U second;

        Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Pair))
                return false;
            Pair<?, ?> other = (Pair<?, ?>) obj;
            return Objects.equals(first, other.first) && Objects.equals(second, other.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }

}
