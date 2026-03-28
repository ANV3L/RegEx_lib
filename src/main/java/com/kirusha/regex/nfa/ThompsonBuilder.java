package com.kirusha.regex.nfa;

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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Построитель NFA из AST методом Томпсона.
 *
 * Главная идея:
 * каждый узел AST превращается в NFAFragment.
 *
 * Затем фрагменты соединяются по правилам Томпсона:
 * - Literal -> один символьный переход
 * - Epsilon -> один epsilon-переход
 * - Concat -> соединение двух фрагментов
 * - Alternation -> ветвление через epsilon
 * - KleeneStar -> цикл через epsilon
 * - Repeat -> n-кратная конкатенация
 * - CharClass -> несколько символьных переходов из одного start в один accept
 * - Group -> на первом этапе просто делегирует child
 *
 * После построения корневого фрагмента:
 * - собираются все достижимые состояния;
 * - собирается алфавит;
 * - формируется объект NFA.
 */
public class ThompsonBuilder {

    /**
     * Счётчик для выдачи уникальных id состояниям.
     *
     * Каждый новый NFAState должен получать новый id.
     */
    private int stateCounter;

    /**
     * Главный вход в построитель.
     *
     * Должен:
     * 1. проверить входной ParserResult;
     * 2. сбросить stateCounter;
     * 3. построить корневой NFAFragment;
     * 4. собрать все состояния автомата;
     * 5. собрать алфавит;
     * 6. вернуть готовый NFA.
     *
     * @param parserResult результат парсинга regex
     * @return построенный NFA
     */
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

    /**
     * Рекурсивно строит NFAFragment для конкретного узла AST.
     *
     * @param node узел AST
     * @return фрагмент NFA для данного узла
     */
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

    /**
     * Строит фрагмент для LiteralNode.
     *
     * Схема:
     *   start --symbol--> accept
     *
     * @param node LiteralNode
     * @return NFAFragment
     */
    private NFAFragment buildLiteral(LiteralNode node) {
        NFAState start = newState();
        NFAState accept = newState();
        start.addTransition(node.getValue(), accept);
        return new NFAFragment(start, accept);
    }

    /**
     * Строит фрагмент для EpsilonNode.
     *
     * Схема:
     *   start --ε--> accept
     *
     * @param node EpsilonNode
     * @return NFAFragment
     */
    private NFAFragment buildEpsilon(EpsilonNode node) {
        NFAState start = newState();
        NFAState accept = newState();
        start.addEpsilonTransition(accept);
        return new NFAFragment(start, accept);
    }

    /**
     * Строит фрагмент для AlternationNode.
     *
     * Схема Томпсона:
     *
     *              ε -> left.start
     * newStart
     *              ε -> right.start
     *
     * left.accept  -> ε -> newAccept
     * right.accept -> ε -> newAccept
     *
     * @param node AlternationNode
     * @return NFAFragment
     */
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

    /**
     * Строит фрагмент для ConcatenationNode.
     *
     * Схема:
     *   leftFragment.accept --ε--> rightFragment.start
     *
     * Результирующий fragment:
     *   start  = leftFragment.start
     *   accept = rightFragment.accept
     *
     * @param node ConcatenationNode
     * @return NFAFragment
     */
    private NFAFragment buildConcatenation(ConcatenationNode node) {
        NFAFragment leftFragment = buildFragment(node.getLeft());
        NFAFragment rightFragment = buildFragment(node.getRight());

        // Optimization: if left is epsilon fragment (start -> ε -> accept with no other transitions),
        // just use right fragment's structure
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

    /**
     * Строит фрагмент для KleeneStarNode.
     *
     * Схема Томпсона:
     *
     * newStart -> ε -> child.start
     * newStart -> ε -> newAccept
     * child.accept -> ε -> child.start
     * child.accept -> ε -> newAccept
     *
     * @param node KleeneStarNode
     * @return NFAFragment
     */
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

    /**
     * Строит фрагмент для RepeatNode.
     *
     * repeat(r, n) = r r r ... r   (n раз)
     *
     * Возможные случаи:
     * - n == 0 -> epsilon
     * - n > 0  -> последовательная конкатенация n копий child
     *
     * Важно:
     * child AST не должен мутироваться.
     *
     * @param node RepeatNode
     * @return NFAFragment
     */
    private NFAFragment buildRepeat(RepeatNode node) {
        int count = node.getCount();

        if (count == 0) {
            return buildEpsilon(new EpsilonNode());
        }

        // Построить первую копию
        NFAFragment result = buildFragment(node.getChild());

        // Для остальных count-1 раз строить и конкатенировать
        for (int i = 1; i < count; i++) {
            NFAFragment next = buildFragment(node.getChild());
            result.getAccept().addEpsilonTransition(next.getStart());
            result = new NFAFragment(result.getStart(), next.getAccept());
        }

        return result;
    }

    /**
     * Строит фрагмент для CharClassNode.
     *
     * Схема:
     *   start --a--> accept
     *   start --b--> accept
     *   start --c--> accept
     *
     * Если набор пустой:
     * - можно трактовать как epsilon,
     *   если это согласовано с parser/ast.
     *
     * @param node CharClassNode
     * @return NFAFragment
     */
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

    /**
     * Строит фрагмент для GroupNode.
     *
     * На первом этапе можно просто делегировать buildFragment(child),
     * то есть группа влияет только на структуру AST, но не меняет форму NFA.
     *
     * В будущем здесь можно:
     * - добавлять метки открытия/закрытия групп;
     * - расширять epsilon-переходы специальной семантикой.
     *
     * @param node GroupNode
     * @return NFAFragment
     */
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

    /**
     * Строит фрагмент для BackReferenceNode.
     *
     * Важно:
     * обратные ссылки не являются регулярной конструкцией
     * в классическом смысле.
     *
     * Поэтому на этапе ThompsonBuilder можно выбрать одну из стратегий:
     * 1. пока не поддерживать и выбрасывать исключение;
     * 2. реализовать позже отдельным движком;
     * 3. помечать как unsupported.
     *
     * На данном этапе рекомендуется выбрасывать UnsupportedOperationException
     * с понятным сообщением.
     *
     * @param node BackReferenceNode
     * @return никогда не возвращает нормальный fragment, если backreference не поддержан
     */
    private NFAFragment buildBackReference(BackReferenceNode node) {
        NFAState start = newState();
        NFAState accept = newState();
        start.addBackrefTransition(node.getGroupNumber(), accept);
        return new NFAFragment(start, accept);
    }

    /**
     * Создаёт новое состояние с уникальным id.
     *
     * @return новый NFAState
     */
    private NFAState newState() {
        return new NFAState(stateCounter++);
    }

    /**
     * Собирает все состояния автомата, достижимые из start.
     *
     * Нужен обход графа:
     * - по символьным переходам;
     * - по epsilon-переходам.
     *
     * @param start стартовое состояние
     * @return множество всех достижимых состояний
     */
    private Set<NFAState> collectStates(NFAState start) {
        Set<NFAState> visited = new HashSet<>();
        Deque<NFAState> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            NFAState current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }

            // Обход по epsilon-переходам
            for (NFAState target : current.getEpsilonTransitions()) {
                if (!visited.contains(target)) {
                    stack.push(target);
                }
            }

            // Обход по символьным переходам
            for (Set<NFAState> targets : current.getTransitions().values()) {
                for (NFAState target : targets) {
                    if (!visited.contains(target)) {
                        stack.push(target);
                    }
                }
            }

            // Обход по backref-переходам
            for (NFAState target : current.getBackrefTransitions().values()) {
                if (!visited.contains(target)) {
                    stack.push(target);
                }
            }
        }

        return visited;
    }

    /**
     * Собирает алфавит автомата по множеству состояний.
     *
     * В алфавит входят только символы символьных переходов.
     * Epsilon туда не входит.
     *
     * @param states множество состояний автомата
     * @return множество символов алфавита
     */
    private Set<String> collectAlphabet(Set<NFAState> states) {
        Set<String> alphabet = new HashSet<>();
        for (NFAState state : states) {
            alphabet.addAll(state.getTransitions().keySet());
        }
        return alphabet;
    }
}
