package com.kirusha.regex.operations;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.SubsetConstructor;
import com.kirusha.regex.engine.DFAEngine;
import com.kirusha.regex.lexer.Lexer;
import com.kirusha.regex.nfa.NFA;
import com.kirusha.regex.nfa.ThompsonBuilder;
import com.kirusha.regex.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование пакета Operations (product construction).
 */
@DisplayName("Operations")
class OperationsTest {

    private Lexer lexer;
    private Parser parser;
    private ThompsonBuilder builder;
    private SubsetConstructor subsetConstructor;
    private DFAOperations dfaOperations;
    private DFAIsomorphism dfaIsomorphism;
    private DFAEngine engine;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
        parser = new Parser();
        builder = new ThompsonBuilder();
        subsetConstructor = new SubsetConstructor();
        dfaOperations = new DFAOperations();
        dfaIsomorphism = new DFAIsomorphism();
        engine = new DFAEngine();
    }

    private DFA compileDFA(String regex) {
        NFA nfa = builder.build(parser.parse(lexer.tokenize(regex)));
        return subsetConstructor.convert(nfa);
    }

    // ==========================================
    // 1. Унарные операции
    // ==========================================

    @Nested
    @DisplayName("1. Унарные операции")
    class UnaryTests {

        @Test
        @DisplayName("Дополнение 'a' отвергает 'a', принимает всё остальное ('', 'aa')")
        void complementTest() {
            DFA dfaA = compileDFA("a");
            DFA compDFA = dfaOperations.complement(dfaA);

            assertTrue(engine.matches(compDFA, ""));
            assertTrue(engine.matches(compDFA, "aa"));
            assertFalse(engine.matches(compDFA, "a"));
        }

        @Test
        @DisplayName("Операция инверсии выбрасывает исключение UnsupportedOperationException")
        void invertTest() {
            DFA dfaA = compileDFA("a");
            assertThrows(UnsupportedOperationException.class, () -> dfaOperations.invert(dfaA));
        }
    }

    // ==========================================
    // 2. Бинарные операции (Product Construction)
    // ==========================================

    @Nested
    @DisplayName("2. Бинарные операции")
    class BinaryTests {

        @Test
        @DisplayName("Пересечение 'a|b' и 'a|c' -> 'a'")
        void intersectionTest() {
            DFA a = compileDFA("a|b");
            DFA b = compileDFA("a|c");
            DFA inter = dfaOperations.intersect(a, b);

            assertTrue(engine.matches(inter, "a"));
            assertFalse(engine.matches(inter, "b"));
            assertFalse(engine.matches(inter, "c"));
            assertFalse(engine.matches(inter, "ac"));
            assertFalse(engine.matches(inter, ""));
        }

        @Test
        @DisplayName("Разность 'a|b' \\ 'a|c' -> 'b'")
        void differenceTest() {
            DFA a = compileDFA("a|b");
            DFA b = compileDFA("a|c");
            DFA diff = dfaOperations.difference(a, b);

            assertFalse(engine.matches(diff, "a"));
            assertTrue(engine.matches(diff, "b"));
            assertFalse(engine.matches(diff, "c"));
        }

        @Test
        @DisplayName("Объединение 'a' ∪ 'b' -> 'a|b'")
        void unionTest() {
            DFA a = compileDFA("a");
            DFA b = compileDFA("b");
            DFA unionDfa = dfaOperations.union(a, b);

            assertTrue(engine.matches(unionDfa, "a"));
            assertTrue(engine.matches(unionDfa, "b"));
            assertFalse(engine.matches(unionDfa, "c"));
            assertFalse(engine.matches(unionDfa, "ab"));
        }

        @Test
        @DisplayName("Сложное пересечение (a|b)* ∩ (b|c)* -> b*")
        void complexIntersection() {
            DFA a = compileDFA("(a|b)*");
            DFA b = compileDFA("(b|c)*");
            DFA inter = dfaOperations.intersect(a, b);

            assertTrue(engine.matches(inter, ""));
            assertTrue(engine.matches(inter, "b"));
            assertTrue(engine.matches(inter, "bb"));
            assertFalse(engine.matches(inter, "a"));
            assertFalse(engine.matches(inter, "c"));
            assertFalse(engine.matches(inter, "ab"));
        }
    }

    // ==========================================
    // 3. Изоморфизм и эквивалентность
    // ==========================================

    @Nested
    @DisplayName("3. Изоморфизм и эквивалентность")
    class IsomorphismTests {

        @Test
        @DisplayName("Автоматы '(a|b)' и '(b|a)' эквивалентны")
        void structurallyIdentical() {
            DFA dfa1 = compileDFA("a|b");
            DFA dfa2 = compileDFA("b|a");
            
            boolean isEquiv = dfaIsomorphism.areEquivalent(dfa1, dfa2);

            assertTrue(isEquiv, "'(a|b)' должно быть эквивалентно '(b|a)'");
        }

        @Test
        @DisplayName("Автоматы 'a' и 'a|a' эквивалентны через разность")
        void areEquivalent() {
            DFA dfa1 = compileDFA("a");
            DFA dfa2 = compileDFA("a|a");

            assertTrue(dfaIsomorphism.areEquivalent(dfa1, dfa2));
        }

        @Test
        @DisplayName("Автоматы 'a' и 'b' не эквивалентны")
        void notEquivalent() {
            DFA dfa1 = compileDFA("a");
            DFA dfa2 = compileDFA("b");

            assertFalse(dfaIsomorphism.areEquivalent(dfa1, dfa2));
        }
    }
}
