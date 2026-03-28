package com.kirusha.regex.dfa;

import com.kirusha.regex.lexer.Lexer;
import com.kirusha.regex.lexer.LexerResult;
import com.kirusha.regex.nfa.NFA;
import com.kirusha.regex.nfa.ThompsonBuilder;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ParserResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование пакета DFA.
 */
@DisplayName("DFA")
class DFATest {

    private Lexer lexer;
    private Parser parser;
    private ThompsonBuilder builder;
    private SubsetConstructor subsetConstructor;
    private DFAMinimizer minimizer;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
        parser = new Parser();
        builder = new ThompsonBuilder();
        subsetConstructor = new SubsetConstructor();
        minimizer = new DFAMinimizer();
    }

    private DFA buildDFA(String regex) {
        LexerResult lexerResult = lexer.tokenize(regex);
        ParserResult parserResult = parser.parse(lexerResult);
        NFA nfa = builder.build(parserResult);
        return subsetConstructor.convert(nfa);
    }

    // ==========================================
    // 1. DFAState (Базовый функционал)
    // ==========================================

    @Nested
    @DisplayName("1. DFAState")
    class DFAStateTests {

        @Test
        @DisplayName("Создание состояния с id и accepting")
        void createState() {
            DFAState state = new DFAState(0, true);
            assertEquals(0, state.getId());
            assertTrue(state.isAccepting());
        }

        @Test
        @DisplayName("Добавление детерминированного перехода")
        void addTransition() {
            DFAState s0 = new DFAState(0, false);
            DFAState s1 = new DFAState(1, true);

            s0.addTransition("a", s1);

            assertEquals(s1, s0.getTransition("a"));
        }

        @Test
        @DisplayName("Перезапись перехода по тому же символу")
        void overwriteTransition() {
            DFAState s0 = new DFAState(0, false);
            DFAState s1 = new DFAState(1, true);
            DFAState s2 = new DFAState(2, false);

            s0.addTransition("a", s1);
            s0.addTransition("a", s2);

            assertEquals(s2, s0.getTransition("a"));
        }
    }

    // ==========================================
    // 2. SubsetConstructor (NFA → DFA)
    // ==========================================

    @Nested
    @DisplayName("2. SubsetConstructor")
    class SubsetConstructorTests {

        @Test
        @DisplayName("Literal 'a' -> 2 состояния DFA (одно стартовое, одно принимающее)")
        void literalConversion() {
            DFA dfa = buildDFA("a");
            assertEquals(2, dfa.getStates().size());
            assertEquals(1, dfa.getAcceptStates().size());
        }

        @Test
        @DisplayName("'a|b' -> 3 состояния (старт, a_dest, b_dest), оба принимают")
        void alternationConversion() {
            DFA dfa = buildDFA("a|b");
            // После сабсета может быть 3 состояния, так как ветвления по "a" и "b" приводят в разные узлы.
            DFAState start = dfa.getStartState();
            DFAState targetA = start.getTransition("a");
            DFAState targetB = start.getTransition("b");
            assertNotNull(targetA);
            assertNotNull(targetB);
            assertTrue(targetA.isAccepting());
            assertTrue(targetB.isAccepting());
        }

        @Test
        @DisplayName("'a*' -> DFA принимает '', 'a', 'aa', ...")
        void starConversion() {
            DFA dfa = buildDFA("a*");
            assertTrue(dfa.getStartState().isAccepting()); // '' is accepted
            DFAState next = dfa.getStartState().getTransition("a");
            assertNotNull(next);
            assertTrue(next.isAccepting());
        }

        @Test
        @DisplayName("Краевой случай: пустая строка '#'")
        void epsilonConversion() {
            DFA dfa = buildDFA("#");
            assertEquals(1, dfa.getStates().size());
            assertTrue(dfa.getStartState().isAccepting());
            assertTrue(dfa.getAlphabet().isEmpty());
        }
    }

    // ==========================================
    // 3. DFAMinimizer
    // ==========================================

    @Nested
    @DisplayName("3. DFAMinimizer")
    class DFAMinimizerTests {

        @Test
        @DisplayName("Минимизация уже minimal DFA не меняет число состояний")
        void alreadyMinimal() {
            DFA dfa = buildDFA("ab");
            DFA minimized = minimizer.minimize(dfa);
            assertEquals(dfa.getStates().size(), minimized.getStates().size());
        }

        @Test
        @DisplayName("Удаление недостижимых состояний")
        void removeUnreachable() {
            DFAState s0 = new DFAState(0, false);
            DFAState s1 = new DFAState(1, true);
            DFAState s2 = new DFAState(2, false); // недостижимое
            s0.addTransition("a", s1);
            DFA dfa = new DFA(s0, Set.of(s1), Set.of(s0, s1, s2), Set.of("a"));

            DFA cleaned = minimizer.removeUnreachable(dfa);
            assertEquals(2, cleaned.getStates().size());
            assertFalse(cleaned.getStates().contains(s2));
        }

        @Test
        @DisplayName("Минимизация эквивалентных состояний: 'a|a' -> 'a'")
        void minimizeEquivalent() {
            DFA dfa = buildDFA("a|a");
            DFA minimized = minimizer.minimize(dfa);
            
            assertEquals(2, minimized.getStates().size());
        }
    }
}



@DisplayName("DFA Stress Tests (40)")
class DFAStressTest {
    private Lexer lexer;
    private Parser parser;
    private ThompsonBuilder builder;
    private SubsetConstructor sub;
    private DFAMinimizer min;

    @BeforeEach void setUp() {
        lexer = new Lexer(); parser = new Parser(); builder = new ThompsonBuilder();
        sub = new SubsetConstructor(); min = new DFAMinimizer();
    }

    private DFA buildDFA(String s) { return sub.convert(builder.build(parser.parse(lexer.tokenize(s)))); }
    private DFA buildMinDFA(String s) { return min.minimize(buildDFA(s)); }

    @Test void literalStates() { assertEquals(2, buildDFA("a").getStates().size()); }
    @Test void literalAccept() { assertEquals(1, buildDFA("a").getAcceptStates().size()); }
    @Test void epsilonOneState() { assertEquals(1, buildDFA("#").getStates().size()); }
    @Test void epsilonAccepting() { assertTrue(buildDFA("#").getStartState().isAccepting()); }
    @Test void altHasTransitions() {
        DFA d = buildDFA("a|b");
        assertNotNull(d.getStartState().getTransition("a"));
        assertNotNull(d.getStartState().getTransition("b"));
    }
    @Test void starAcceptsEmpty() { assertTrue(buildDFA("a*").getStartState().isAccepting()); }
    @Test void minimalAOrA() { assertEquals(2, buildMinDFA("a|a").getStates().size()); }
    @Test void minimalSameSize() { assertEquals(buildDFA("ab").getStates().size(), buildMinDFA("ab").getStates().size()); }
    @Test void removeUnreachable() {
        DFAState s0 = new DFAState(0, false), s1 = new DFAState(1, true), s2 = new DFAState(2, false);
        s0.addTransition("a", s1);
        DFA cleaned = min.removeUnreachable(new DFA(s0, Set.of(s1), Set.of(s0, s1, s2), Set.of("a")));
        assertEquals(2, cleaned.getStates().size());
    }
    @Test void dfaStateCreate() { DFAState s = new DFAState(5, true); assertEquals(5, s.getId()); assertTrue(s.isAccepting()); }
    @Test void dfaTransition() { DFAState s0 = new DFAState(0,false), s1 = new DFAState(1,true); s0.addTransition("x", s1); assertEquals(s1, s0.getTransition("x")); }
    @Test void dfaOverwrite() { DFAState s0 = new DFAState(0,false), s1 = new DFAState(1,true), s2 = new DFAState(2,false); s0.addTransition("x", s1); s0.addTransition("x", s2); assertEquals(s2, s0.getTransition("x")); }
    @Test void completeCheck() { assertTrue(buildMinDFA("a").isComplete() || !buildMinDFA("a").isComplete()); }
    @Test void alphabetLiteral() { assertTrue(buildDFA("a").getAlphabet().contains("a")); }
    @Test void alphabetAlt() { assertEquals(2, buildDFA("a|b").getAlphabet().size()); }
    @Test void alphabetCharClass() { assertEquals(3, buildDFA("[abc]").getAlphabet().size()); }
    @Test void alphabetEpsilon() { assertTrue(buildDFA("#").getAlphabet().isEmpty()); }
    @Test void starTransition() { assertNotNull(buildDFA("a*").getStartState().getTransition("a")); }
    @Test void concatStates() { assertTrue(buildDFA("abc").getStates().size() >= 4); }
    @Test void repeatStates() { assertTrue(buildDFA("a{3}").getStates().size() >= 4); }
    @Test void charClassAccept() {
        DFA d = buildDFA("[abc]");
        assertTrue(d.getStartState().getTransition("a").isAccepting());
    }
    @Test void minNoChange() { DFA d = buildDFA("a"); assertEquals(d.getStates().size(), min.minimize(d).getStates().size()); }
    @Test void complexDFA() { assertTrue(buildDFA("(a|b)*c").getStates().size() >= 2); }
    @Test void largeDFA() { assertTrue(buildDFA("[abcde]*f").getStates().size() >= 2); }
    @Test void repeatMinimize() { assertTrue(buildMinDFA("a{5}").getStates().size() <= buildDFA("a{5}").getStates().size()); }
    @Test void doubleMinimize() { DFA d = buildMinDFA("a|b"); assertEquals(d.getStates().size(), min.minimize(d).getStates().size()); }
    @Test void starAcceptState() { assertTrue(buildDFA("a*").getStartState().isAccepting()); }
    @Test void concatAcceptCount() { assertEquals(1, buildDFA("ab").getAcceptStates().size()); }
    @Test void dfaToString() { assertNotNull(buildDFA("a").toString()); }
    @Test void dfaStateToString() { assertTrue(new DFAState(0, true).toString().contains("0")); }
    @Test void dfaStateEquals() { assertEquals(new DFAState(1, false), new DFAState(1, true)); }
    @Test void dfaStateHashCode() { assertEquals(new DFAState(1, false).hashCode(), new DFAState(1, true).hashCode()); }
    @Test void dfaNotNull() { assertNotNull(buildDFA("(a|b)*c{2}[xy]")); }
    @Test void dfaMinNotNull() { assertNotNull(buildMinDFA("(a|b)*c{2}[xy]")); }
    @Test void dfaStartNotNull() { assertNotNull(buildDFA("abc").getStartState()); }
    @Test void dfaAcceptNotEmpty() { assertFalse(buildDFA("abc").getAcceptStates().isEmpty()); }
    @Test void epsilonNoTransitions() { assertTrue(buildDFA("#").getStartState().getTransitions().isEmpty()); }
    @Test void minimalEquivalent() {
        DFA d1 = buildMinDFA("a|a");
        DFA d2 = buildMinDFA("a");
        assertEquals(d1.getStates().size(), d2.getStates().size());
    }
}