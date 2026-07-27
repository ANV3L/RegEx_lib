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

    // =================== extra methods ========

    public ASTNode reverseAST(ASTNode node) {
        if (node == null) {
            return null;
        }

        if (node instanceof LiteralNode ||
                node instanceof EpsilonNode ||
                node instanceof CharClassNode ||
                node instanceof BackReferenceNode) {
            return node;
        }

        if (node instanceof ConcatenationNode) {
            ConcatenationNode concat = (ConcatenationNode) node;
            return new ConcatenationNode(
                    reverseAST(concat.getRight()),
                    reverseAST(concat.getLeft()));
        }

        if (node instanceof AlternationNode) {
            AlternationNode alt = (AlternationNode) node;
            return new AlternationNode(
                    reverseAST(alt.getLeft()),
                    reverseAST(alt.getRight()));
        }

        if (node instanceof KleeneStarNode) {
            return new KleeneStarNode(
                    reverseAST(((KleeneStarNode) node).getChild()));
        }

        if (node instanceof RepeatNode) {
            RepeatNode repeat = (RepeatNode) node;
            return new RepeatNode(
                    reverseAST(repeat.getChild()),
                    repeat.getCount());
        }

        if (node instanceof GroupNode) {
            GroupNode group = (GroupNode) node;
            return new GroupNode(
                    group.getGroupNumber(),
                    reverseAST(group.getChild()));
        }

        return null;

    }

    // Опциональная версия (?)
    public ASTNode makeOptional(ASTNode node) {
        return new AlternationNode(node, new EpsilonNode());
    }

    // положительное замыкание клини
    public ASTNode makePositiveClosure(ASTNode node) {
        return new ConcatenationNode(node, new KleeneStarNode(node));
    }

    // повторить выражения от min до max раз
    public ASTNode makeRangeRepeat(ASTNode node, int min, int max) {
        if (min > max)
            throw new IllegalArgumentException("min > max");

        if (min == 0 && max == 0) {
            return new EpsilonNode();
        }

        ASTNode result = null;
        for (int i = 0; i < min; i++) {
            ASTNode copy = copyAST(node);
            result = (result == null) ? copy : new ConcatenationNode(result, copy);
        }

        for (int i = min; i < max; i++) {
            ASTNode copy = copyAST(node);
            ASTNode optional = makeOptional(copy);
            result = (result == null) ? optional : new ConcatenationNode(result, optional);
        }

        return result != null ? result : new EpsilonNode();
    }

    public ASTNode copyAST(ASTNode node) {
        if (node == null)
            return null;

        if (node instanceof LiteralNode) {
            return new LiteralNode(((LiteralNode) node).getValue());
        }
        if (node instanceof EpsilonNode) {
            return new EpsilonNode();
        }
        if (node instanceof CharClassNode) {
            return new CharClassNode(((CharClassNode) node).getSymbols());
        }
        if (node instanceof ConcatenationNode) {
            ConcatenationNode concat = (ConcatenationNode) node;
            return new ConcatenationNode(copyAST(concat.getLeft()), copyAST(concat.getRight()));
        }
        if (node instanceof AlternationNode) {
            AlternationNode alt = (AlternationNode) node;
            return new AlternationNode(copyAST(alt.getLeft()), copyAST(alt.getRight()));
        }
        if (node instanceof KleeneStarNode) {
            return new KleeneStarNode(copyAST(((KleeneStarNode) node).getChild()));
        }
        if (node instanceof RepeatNode) {
            RepeatNode repeat = (RepeatNode) node;
            return new RepeatNode(copyAST(repeat.getChild()), repeat.getCount());
        }
        if (node instanceof GroupNode) {
            GroupNode group = (GroupNode) node;
            return new GroupNode(group.getGroupNumber(), copyAST(group.getChild()));
        }

        return null;
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


    // Звездочка клини
    private void addAllTransitions(NFAState from, NFAState to, Set<String> alphabet) {
        for (String symbol : alphabet) {
            from.addTransition(symbol, to);
        }
    }

    private NFAState createPath(NFAState from, NFAState to, String path) {
        if (path.isEmpty()) {
            from.addEpsilonTransition(to);
            return from;
        }

        NFAState current = from;
        for (int i = 0; i < path.length() - 1; i++) {
            NFAState next = newState();
            current.addTransition(String.valueOf(path.charAt(i)), next);
            current = next;
        }

        current.addTransition(String.valueOf(path.charAt(path.length() - 1)), to);

        return from;
    }

    public ASTNode createSubstring(ASTNode node) {
        ASTNode anyChar = new CharClassNode(java.util.Arrays.asList(".", "*"));
        ASTNode anyStar = new KleeneStarNode(anyChar);
        return new ConcatenationNode(
                new ConcatenationNode(anyStar, node),
                anyStar);
    }

    public ASTNode makePalindrome(ASTNode node) {
        return new ConcatenationNode(node, reverseAST(node));
    }

    public ASTNode powerAST(ASTNode node, int n) {
        if (n <= 0)
            return new EpsilonNode();
        if (n == 1)
            return copyAST(node);

        ASTNode result = copyAST(node);
        for (int i = 1; i < n; i++) {
            result = new ConcatenationNode(result, copyAST(node));
        }
        return result;
    }

    public NFAFragment complementNFAFragment(NFAFragment fragment, Set<String> alphabet) {
        if (fragment == null || alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("Fragment and alphabet cannot be null or empty");
        }

        Set<NFAState> allStates = collectStatesFromFragment(fragment);

        Map<NFAState, NFAState> stateMapping = new HashMap<>();
        NFAState newStart = newState();
        NFAState trapState = newState();

        for (NFAState state : allStates) {
            stateMapping.put(state, newState());
        }

        NFAState complementStart = stateMapping.get(fragment.getStart());
        if (complementStart == null) {
            complementStart = newStart;
            stateMapping.put(fragment.getStart(), complementStart);
        }

        for (NFAState originalState : allStates) {
            NFAState newState = stateMapping.get(originalState);

            for (Map.Entry<String, Set<NFAState>> entry : originalState.getTransitions().entrySet()) {
                String symbol = entry.getKey();
                for (NFAState target : entry.getValue()) {
                    NFAState newTarget = stateMapping.get(target);
                    if (newTarget != null) {
                        newState.addTransition(symbol, newTarget);
                    }
                }
            }

            for (NFAState target : originalState.getEpsilonTransitions()) {
                NFAState newTarget = stateMapping.get(target);
                if (newTarget != null) {
                    newState.addEpsilonTransition(newTarget);
                }
            }

            Set<String> definedSymbols = originalState.getTransitions().keySet();
            for (String symbol : alphabet) {
                if (!definedSymbols.contains(symbol)) {
                    newState.addTransition(symbol, trapState);
                }
            }
        }

        for (String symbol : alphabet) {
            trapState.addTransition(symbol, trapState);
        }

        NFAState complementAccept = newState();

        for (NFAState originalState : allStates) {
            if (!originalState.equals(fragment.getAccept())) {
                NFAState newState = stateMapping.get(originalState);
                if (newState != null) {
                    newState.addEpsilonTransition(complementAccept);
                }
            }
        }

        trapState.addEpsilonTransition(complementAccept);

        return new NFAFragment(complementStart, complementAccept);
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

    public ASTNode complementAST(ASTNode node, Set<String> alphabet) {
        if (node instanceof LiteralNode) {
            LiteralNode literal = (LiteralNode) node;

            java.util.List<String> complementSymbols = new java.util.ArrayList<>();
            for (String symbol : alphabet) {
                if (!symbol.equals(literal.getValue())) {
                    complementSymbols.add(symbol);
                }
            }

            if (complementSymbols.isEmpty()) {
                return new EpsilonNode();
            }

            return new CharClassNode(complementSymbols);
        }

        if (node instanceof EpsilonNode) {
            ASTNode anyChar = createAnyChar(alphabet);
            return makePositiveClosure(anyChar);
        }

        if (node instanceof CharClassNode) {
            CharClassNode charClass = (CharClassNode) node;

            java.util.List<String> complementSymbols = new java.util.ArrayList<>();
            Set<String> originalSymbols = new HashSet<>(charClass.getSymbols());

            for (String symbol : alphabet) {
                if (!originalSymbols.contains(symbol)) {
                    complementSymbols.add(symbol);
                }
            }

            return new CharClassNode(complementSymbols);
        }

        if (node instanceof AlternationNode) {
            AlternationNode alt = (AlternationNode) node;
            return node;
        }

        return node;
    }

    private ASTNode createAnyChar(Set<String> alphabet) {
        if (alphabet.isEmpty()) {
            return new EpsilonNode();
        }

        return new CharClassNode(new java.util.ArrayList<>(alphabet));
    }

    public NFAFragment complementViaDFA(NFAFragment fragment, Set<String> alphabet) {
        return complementNFAFragment(fragment, alphabet);
    }

     private Set<NFAState> moveAndClose(Set<NFAState> states, String symbol) {
        Set<NFAState> moved = new HashSet<>();
        for (NFAState s : states) {
            Set<NFAState> targets = s.getTransitions().getOrDefault(symbol, Collections.emptySet());
            moved.addAll(targets);
        }
        return moved.isEmpty() ? Collections.emptySet() : getEpsilonClosureSet(moved);
    }

    private Set<NFAState> getEpsilonClosureTargets(NFAState state, String symbol) {
        if (state == null) {
            return Collections.emptySet();
        }

        Set<NFAState> closure = getEpsilonClosure(state);
        Set<NFAState> targets = new HashSet<>();

        for (NFAState s : closure) {
            Set<NFAState> directTargets = s.getTransitions().getOrDefault(symbol, Collections.emptySet());
            for (NFAState target : directTargets) {
                targets.addAll(getEpsilonClosure(target));
            }
        }

        return targets;
    }

    private Set<NFAState> getEpsilonClosure(NFAState state) {
        if (state == null) {
            return Collections.emptySet();
        }

        Set<NFAState> closure = new HashSet<>();
        Deque<NFAState> stack = new ArrayDeque<>();

        closure.add(state);
        stack.push(state);

        while (!stack.isEmpty()) {
            NFAState current = stack.pop();
            for (NFAState target : current.getEpsilonTransitions()) {
                if (closure.add(target)) {
                    stack.push(target);
                }
            }
        }

        return closure;
    }

    private boolean isInEpsilonClosure(NFAState state, NFAState target) {
        if (state == null || target == null) {
            return false;
        }
        return getEpsilonClosure(state).contains(target);
    }


    public NFAFragment xorNFAFragment(NFAFragment fragmentA, NFAFragment fragmentB, Set<String> alphabet) {
        if (fragmentA == null || fragmentB == null) {
            throw new IllegalArgumentException("Fragments cannot be null");
        }

        NFAFragment notA = complementNFAFragment(fragmentA, alphabet);
        NFAFragment notB = complementNFAFragment(fragmentB, alphabet);

        NFAFragment aAndNotB = intersectNFAFragment(fragmentA, notB, alphabet);
        NFAFragment notAAndB = intersectNFAFragment(notA, fragmentB, alphabet);

        return unionNFAFragment(aAndNotB, notAAndB);
    }

    private NFAFragment intersectNFAFragment(NFAFragment fragmentA, NFAFragment fragmentB, Set<String> alphabet) {

        Map<Pair<NFAState, NFAState>, NFAState> stateMapping = new HashMap<>();
        NFAState startA = fragmentA.getStart();
        NFAState startB = fragmentB.getStart();

        NFAState newStart = newState();
        stateMapping.put(new Pair<>(startA, startB), newStart);

        Queue<Pair<NFAState, NFAState>> queue = new ArrayDeque<>();
        queue.add(new Pair<>(startA, startB));

        while (!queue.isEmpty()) {
            Pair<NFAState, NFAState> current = queue.poll();
            NFAState currentNew = stateMapping.get(current);

            for (String symbol : alphabet) {
                Set<NFAState> targetsA = current.first.getTransitions().getOrDefault(symbol, Collections.emptySet());
                Set<NFAState> targetsB = current.second.getTransitions().getOrDefault(symbol, Collections.emptySet());

                for (NFAState targetA : targetsA) {
                    for (NFAState targetB : targetsB) {
                        Pair<NFAState, NFAState> newPair = new Pair<>(targetA, targetB);

                        NFAState newTarget = stateMapping.get(newPair);
                        if (newTarget == null) {
                            newTarget = newState();
                            stateMapping.put(newPair, newTarget);
                            queue.add(newPair);
                        }

                        currentNew.addTransition(symbol, newTarget);
                    }
                }
            }
        }

        NFAState newAccept = newState();
        for (Map.Entry<Pair<NFAState, NFAState>, NFAState> entry : stateMapping.entrySet()) {
            if (entry.getKey().first.equals(fragmentA.getAccept()) &&
                    entry.getKey().second.equals(fragmentB.getAccept())) {
                entry.getValue().addEpsilonTransition(newAccept);
            }
        }

        return new NFAFragment(newStart, newAccept);
    }

    private NFAFragment unionNFAFragment(NFAFragment fragmentA, NFAFragment fragmentB) {
        NFAState newStart = newState();
        NFAState newAccept = newState();

        newStart.addEpsilonTransition(fragmentA.getStart());
        newStart.addEpsilonTransition(fragmentB.getStart());

        fragmentA.getAccept().addEpsilonTransition(newAccept);
        fragmentB.getAccept().addEpsilonTransition(newAccept);

        return new NFAFragment(newStart, newAccept);
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
            LiteralNode literal = (LiteralNode) node;
            alphabet.add(literal.getValue());
            return;
        }

        if (node instanceof CharClassNode) {
            CharClassNode charClass = (CharClassNode) node;
            alphabet.addAll(charClass.getSymbols());
            return;
        }

        if (node instanceof BackReferenceNode) {
            BackReferenceNode backref = (BackReferenceNode) node;
            return;
        }

        if (node instanceof EpsilonNode) {
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
            return;
        }
    }

}
