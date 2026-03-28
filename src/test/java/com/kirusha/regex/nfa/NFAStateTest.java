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
