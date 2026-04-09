package com.kirusha.regex.nfa;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

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
import com.kirusha.regex.parser.ast.RepeatNode;


public class ThompsonBuilder {

    private int stateCounter;

    public NFA build(ParserResult parserResult) {
        if (parserResult == null) {
            throw new IllegalArgumentException("ParserResult must not be null");
        }

        stateCounter = 0;

        ASTNode root = parserResult.getRoot();
        NFAFragment fragment = buildFragment(root);

        Set<NFAState> states = collectStates(fragment.getStart());
        Set<String> alphabet = collectAlphabet(states);

        return new NFA(
                fragment.getStart(),
                fragment.getAccept(),
                states,
                alphabet,
                parserResult.getGroupCount()
        );
    }

    private NFAFragment buildFragment(ASTNode node) {
        if (node instanceof LiteralNode) {
            return buildLiteral((LiteralNode) node);
        } else if (node instanceof EpsilonNode) {
            return buildEpsilon((EpsilonNode) node);
        } else if (node instanceof AlternationNode) {
            return buildAlternation((AlternationNode) node);
        } else if (node instanceof ConcatenationNode) {
            return buildConcatenation((ConcatenationNode) node);
        } else if (node instanceof KleeneStarNode) {
            return buildStar((KleeneStarNode) node);
        } else if (node instanceof RepeatNode) {
            return buildRepeat((RepeatNode) node);
        } else if (node instanceof CharClassNode) {
            return buildCharClass((CharClassNode) node);
        } else if (node instanceof GroupNode) {
            return buildGroup((GroupNode) node);
        } else if (node instanceof BackReferenceNode) {
            return buildBackReference((BackReferenceNode) node);
        } else {
            throw new IllegalArgumentException("Unknown AST node type: " + node.getClass().getSimpleName());
        }
    }

 
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
        NFAFragment leftFragment = buildFragment(node.getLeft());
        NFAFragment rightFragment = buildFragment(node.getRight());

        NFAState newStart = newState();
        NFAState newAccept = newState();

        newStart.addEpsilonTransition(leftFragment.getStart());
        newStart.addEpsilonTransition(rightFragment.getStart());

        leftFragment.getAccept().addEpsilonTransition(newAccept);
        rightFragment.getAccept().addEpsilonTransition(newAccept);

        return new NFAFragment(newStart, newAccept);
    }

    private NFAFragment buildConcatenation(ConcatenationNode node) {
        NFAFragment leftFragment = buildFragment(node.getLeft());
        NFAFragment rightFragment = buildFragment(node.getRight());

        if (isSimpleEpsilon(leftFragment)) {
            return rightFragment;
        }
        if (isSimpleEpsilon(rightFragment)) {
            return leftFragment;
        }

        leftFragment.getAccept().addEpsilonTransition(rightFragment.getStart());

        return new NFAFragment(leftFragment.getStart(), rightFragment.getAccept());
    }

    private boolean isSimpleEpsilon(NFAFragment fragment) {
        NFAState start = fragment.getStart();
        NFAState accept = fragment.getAccept();
        return start.getTransitions().isEmpty() 
            && start.getEpsilonTransitions().size() == 1 
            && start.getEpsilonTransitions().contains(accept)
            && accept.getTransitions().isEmpty()
            && accept.getEpsilonTransitions().isEmpty()
            && start.getGroupOpen() == -1
            && accept.getGroupClose() == -1
            && start.getBackrefTransitions().isEmpty()
            && accept.getBackrefTransitions().isEmpty();
    }

    private NFAFragment buildStar(KleeneStarNode node) {
        NFAFragment childFragment = buildFragment(node.getChild());

        NFAState newStart = newState();
        NFAState newAccept = newState();

        newStart.addEpsilonTransition(childFragment.getStart());
        newStart.addEpsilonTransition(newAccept);

        childFragment.getAccept().addEpsilonTransition(childFragment.getStart());
        childFragment.getAccept().addEpsilonTransition(newAccept);

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
        if (node.isEmpty()) {
            return buildEpsilon(new EpsilonNode());
        }

        NFAState start = newState();
        NFAState accept = newState();

        for (String symbol : node.getSymbols()) {
            start.addTransition(symbol, accept);
        }

        return new NFAFragment(start, accept);
    }

    private NFAFragment buildGroup(GroupNode node) {
        NFAFragment childFragment = buildFragment(node.getChild());
        
        NFAState start = newState();
        start.setGroupOpen(node.getGroupNumber());
        start.addEpsilonTransition(childFragment.getStart());
        
        NFAState accept = newState();
        accept.setGroupClose(node.getGroupNumber());
        childFragment.getAccept().addEpsilonTransition(accept);
        
        return new NFAFragment(start, accept);
    }

    private NFAFragment buildBackReference(BackReferenceNode node) {
        NFAState start = newState();
        NFAState accept = newState();
        start.addBackrefTransition(node.getGroupNumber(), accept);
        return new NFAFragment(start, accept);
    }

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
}
