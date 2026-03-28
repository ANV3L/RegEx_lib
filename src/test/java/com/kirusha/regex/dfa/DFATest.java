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
