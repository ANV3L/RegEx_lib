package com.kirusha.regex.nfa;

import com.kirusha.regex.lexer.Lexer;
import com.kirusha.regex.lexer.LexerResult;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ParserResult;
import com.kirusha.regex.parser.ast.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование NFAState.
 */
@DisplayName("NFAState")
class NFAStateTest {

    // ==========================================
    // 1. СОЗДАНИЕ СОСТОЯНИЯ
    // ==========================================

    @Nested
    @DisplayName("1. Создание состояния")
    class Creation {

        @Test
        @DisplayName("Состояние создаётся с заданным id")
        void createWithId() {
            NFAState state = new NFAState(42);
            assertEquals(42, state.getId());
        }

        @Test
        @DisplayName("Transitions изначально пусты")
        void emptyTransitions() {
            NFAState state = new NFAState(0);
            assertTrue(state.getTransitions().isEmpty());
        }

        @Test
        @DisplayName("Epsilon-transitions изначально пусты")
        void emptyEpsilonTransitions() {
            NFAState state = new NFAState(0);
            assertTrue(state.getEpsilonTransitions().isEmpty());
        }
    }

    // ==========================================
    // 2. СИМВОЛЬНЫЕ ПЕРЕХОДЫ
    // ==========================================

    @Nested
    @DisplayName("2. Символьные переходы")
    class SymbolTransitions {

        @Test
        @DisplayName("Добавление одного перехода по символу")
        void addSingleTransition() {
            NFAState s0 = new NFAState(0);
            NFAState s1 = new NFAState(1);

            s0.addTransition("a", s1);

            Set<NFAState> targets = s0.getTransitions().get("a");
            assertNotNull(targets);
            assertEquals(1, targets.size());
            assertTrue(targets.contains(s1));
        }

        @Test
        @DisplayName("Несколько переходов по одному символу (NFA)")
        void multipleTransitionsSameSymbol() {
            NFAState s0 = new NFAState(0);
            NFAState s1 = new NFAState(1);
            NFAState s2 = new NFAState(2);

            s0.addTransition("a", s1);
            s0.addTransition("a", s2);

            Set<NFAState> targets = s0.getTransitions().get("a");
            assertNotNull(targets);
            assertEquals(2, targets.size());
            assertTrue(targets.contains(s1));
            assertTrue(targets.contains(s2));
        }

        @Test
        @DisplayName("Переходы по разным символам")
        void transitionsDifferentSymbols() {
            NFAState s0 = new NFAState(0);
            NFAState s1 = new NFAState(1);
            NFAState s2 = new NFAState(2);

            s0.addTransition("a", s1);
            s0.addTransition("b", s2);

            assertEquals(2, s0.getTransitions().size());
            assertTrue(s0.getTransitions().containsKey("a"));
            assertTrue(s0.getTransitions().containsKey("b"));
        }
    }

    // ==========================================
    // 3. EPSILON-ПЕРЕХОДЫ
    // ==========================================

    @Nested
    @DisplayName("3. Epsilon-переходы")
    class EpsilonTransitions {

        @Test
        @DisplayName("Добавление epsilon-перехода")
        void addEpsilonTransition() {
            NFAState s0 = new NFAState(0);
            NFAState s1 = new NFAState(1);

            s0.addEpsilonTransition(s1);

            assertEquals(1, s0.getEpsilonTransitions().size());
            assertTrue(s0.getEpsilonTransitions().contains(s1));
        }

        @Test
        @DisplayName("Несколько epsilon-переходов")
        void multipleEpsilonTransitions() {
            NFAState s0 = new NFAState(0);
            NFAState s1 = new NFAState(1);
            NFAState s2 = new NFAState(2);

            s0.addEpsilonTransition(s1);
            s0.addEpsilonTransition(s2);

            assertEquals(2, s0.getEpsilonTransitions().size());
        }
    }

    // ==========================================
    // 4. EQUALS / HASHCODE / TOSTRING
    // ==========================================

    @Nested
    @DisplayName("4. equals / hashCode / toString")
    class EqualsHashCodeToString {

        @Test
        @DisplayName("Состояния с одинаковым id равны")
        void equalById() {
            NFAState s1 = new NFAState(5);
            NFAState s2 = new NFAState(5);
            assertEquals(s1, s2);
            assertEquals(s1.hashCode(), s2.hashCode());
        }

        @Test
        @DisplayName("Состояния с разным id не равны")
        void notEqualByDifferentId() {
            NFAState s1 = new NFAState(1);
            NFAState s2 = new NFAState(2);
            assertNotEquals(s1, s2);
        }

        @Test
        @DisplayName("toString содержит id")
        void toStringContainsId() {
            NFAState state = new NFAState(7);
            assertTrue(state.toString().contains("7"));
        }
    }
}



@DisplayName("NFA/Thompson Stress Tests (40)")
class NFAStressTest {
    private Lexer lexer;
    private Parser parser;
    private ThompsonBuilder builder;

    @BeforeEach void setUp() { lexer = new Lexer(); parser = new Parser(); builder = new ThompsonBuilder(); }

    private NFA build(String s) { return builder.build(parser.parse(lexer.tokenize(s))); }

    @Test void literalStates() { assertEquals(2, build("a").getStates().size()); }
    @Test void literalAlphabet() { assertTrue(build("a").getAlphabet().contains("a")); }
    @Test void epsilonStates() { assertEquals(2, build("~").getStates().size()); }
    @Test void epsilonAlphabet() { assertTrue(build("~").getAlphabet().isEmpty()); }
    @Test void concatStates() { assertEquals(4, build("ab").getStates().size()); }
    @Test void altStates() { assertEquals(6, build("a|b").getStates().size()); }
    @Test void starStates() { assertEquals(4, build("a*").getStates().size()); }
    @Test void repeatThreeStates() { assertEquals(6, build("a{3}").getStates().size()); }
    @Test void charClassStates() { assertEquals(2, build("[abc]").getStates().size()); }
    @Test void charClassAlphabet() { assertEquals(3, build("[abc]").getAlphabet().size()); }
    @Test void emptyCharClass() { assertTrue(build("[]").getAlphabet().isEmpty()); }
    @Test void groupStates() { assertEquals(4, build("(a)").getStates().size()); }
    @Test void groupCount() { assertEquals(2, build("(a)(b)").getGroupCount()); }
    @Test void repeatOneStates() { assertEquals(2, build("a{1}").getStates().size()); }
    @Test void repeatZeroAlphabet() { assertTrue(build("a{0}").getAlphabet().isEmpty()); }
    @Test void startNotNull() { assertNotNull(build("a").getStartState()); }
    @Test void acceptNotNull() { assertNotNull(build("a").getAcceptState()); }
    @Test void startNotAccept() { assertNotEquals(build("a").getStartState(), build("a").getAcceptState()); }
    @Test void starEpsilons() { assertEquals(2, build("a*").getStartState().getEpsilonTransitions().size()); }
    @Test void altEpsilons() { assertEquals(2, build("a|b").getStartState().getEpsilonTransitions().size()); }
    @Test void tripleConcat() { assertEquals(6, build("abc").getStates().size()); }
    @Test void tripleAltAlphabet() { assertEquals(3, build("a|b|c").getAlphabet().size()); }
    @Test void charClassTransitions() { assertTrue(build("[ab]").getStartState().getTransitions().containsKey("a")); }
    @Test void nullParserResult() { assertThrows(IllegalArgumentException.class, () -> builder.build(null)); }
    @Test void complexStates() { assertTrue(build("(a|b)*c").getStates().size() > 6); }
    @Test void complexAlphabet() { assertEquals(3, build("(a|b)*c").getAlphabet().size()); }
    @Test void backrefStates() { assertTrue(build("(a)\\1").getStates().size() > 0); }
    @Test void repeatCharClass() { assertEquals(4, build("[ab]{2}").getStates().size()); }
    @Test void doubleStarAlphabet() { assertEquals(2, build("a*b*").getAlphabet().size()); }
    @Test void groupWithAlt() { assertTrue(build("(a|b)").getAlphabet().contains("b")); }
    @Test void fiveRepeat() { assertEquals(10, build("a{5}").getStates().size()); }
    @Test void concatEpsilon() { assertEquals(6, build("a~b").getStates().size()); }
    @Test void altEpsilon() { assertTrue(build("~|a").getAlphabet().contains("a")); }
    @Test void groupStar() { assertTrue(build("(ab)*").getStates().size() > 4); }
    @Test void escapedLiteral() { assertTrue(build("\\*").getAlphabet().contains("*")); }
    @Test void multiCharClass() { assertEquals(5, build("[abcde]").getAlphabet().size()); }
    @Test void fullCombined() { assertTrue(build("(a|b)*c{2}[xy]~").getAlphabet().size() >= 4); }
    @Test void deepNesting() { assertEquals(3, build("(((a)))").getGroupCount()); }
    @Test void repeatGroup() { assertTrue(build("(ab){3}").getStates().size() > 6); }
    @Test void largeRepeat() { assertEquals(20, build("a{10}").getStates().size()); }
}