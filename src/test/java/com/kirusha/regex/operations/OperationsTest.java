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



@DisplayName("Operations Stress Tests (40)")
class OperationsStressTest {
    private DFAOperations ops;
    private DFAIsomorphism iso;
    private DFAEngine engine;
    private Lexer lexer; private Parser parser; private ThompsonBuilder builder; private SubsetConstructor sub;

    @BeforeEach void setUp() {
        ops = new DFAOperations(); iso = new DFAIsomorphism(); engine = new DFAEngine();
        lexer = new Lexer(); parser = new Parser(); builder = new ThompsonBuilder(); sub = new SubsetConstructor();
    }

    private DFA compile(String s) { return sub.convert(builder.build(parser.parse(lexer.tokenize(s)))); }

    // Intersection
    @Test void interSimple() { assertTrue(engine.matches(ops.intersect(compile("a|b"), compile("a|c")), "a")); }
    @Test void interReject() { assertFalse(engine.matches(ops.intersect(compile("a|b"), compile("a|c")), "b")); }
    @Test void interEmpty() { assertFalse(engine.matches(ops.intersect(compile("a"), compile("b")), "a")); }
    @Test void interStar() { assertTrue(engine.matches(ops.intersect(compile("(a|b)*"), compile("a*")), "aaa")); }
    @Test void interStarReject() { assertFalse(engine.matches(ops.intersect(compile("(a|b)*"), compile("a*")), "b")); }

    // Difference
    @Test void diffSimple() { assertTrue(engine.matches(ops.difference(compile("a|b"), compile("a")), "b")); }
    @Test void diffReject() { assertFalse(engine.matches(ops.difference(compile("a|b"), compile("a")), "a")); }
    @Test void diffSame() { assertFalse(engine.matches(ops.difference(compile("a"), compile("a")), "a")); }
    @Test void diffStar() { assertTrue(engine.matches(ops.difference(compile("(a|b)*"), compile("a*")), "b")); }
    @Test void diffStarReject() { assertFalse(engine.matches(ops.difference(compile("(a|b)*"), compile("a*")), "")); }

    // Union
    @Test void unionSimple() { assertTrue(engine.matches(ops.union(compile("a"), compile("b")), "a")); }
    @Test void unionSimple2() { assertTrue(engine.matches(ops.union(compile("a"), compile("b")), "b")); }
    @Test void unionReject() { assertFalse(engine.matches(ops.union(compile("a"), compile("b")), "c")); }
    @Test void unionStar() { assertTrue(engine.matches(ops.union(compile("a*"), compile("b*")), "bbb")); }
    @Test void unionEmpty() { assertTrue(engine.matches(ops.union(compile("a*"), compile("b*")), "")); }

    // Complement
    @Test void compAccept() { assertTrue(engine.matches(ops.complement(compile("a")), "")); }
    @Test void compReject() { assertFalse(engine.matches(ops.complement(compile("a")), "a")); }
    @Test void compLong() { assertTrue(engine.matches(ops.complement(compile("a")), "aa")); }
    @Test void compStar() { assertFalse(engine.matches(ops.complement(compile("a*")), "aaa")); }

    // Inversion
    @Test void invertThrows() { assertThrows(UnsupportedOperationException.class, () -> ops.invert(compile("a"))); }

    // Isomorphism / Equivalence
    @Test void equivSame() { assertTrue(iso.areEquivalent(compile("a"), compile("a"))); }
    @Test void equivAltOrder() { assertTrue(iso.areEquivalent(compile("a|b"), compile("b|a"))); }
    @Test void equivDuplicate() { assertTrue(iso.areEquivalent(compile("a"), compile("a|a"))); }
    @Test void notEquiv() { assertFalse(iso.areEquivalent(compile("a"), compile("b"))); }
    @Test void isoSame() { assertTrue(iso.areIsomorphic(compile("a"), compile("a"))); }

    // Non-mutation
    @Test void intersectNoMutate() {
        DFA a = compile("a|b"), b = compile("a|c");
        int sizeA = a.getStates().size(), sizeB = b.getStates().size();
        ops.intersect(a, b);
        assertEquals(sizeA, a.getStates().size());
        assertEquals(sizeB, b.getStates().size());
    }
    @Test void diffNoMutate() {
        DFA a = compile("a|b"), b = compile("a");
        int size = a.getStates().size();
        ops.difference(a, b);
        assertEquals(size, a.getStates().size());
    }
    @Test void unionNoMutate() {
        DFA a = compile("a"), b = compile("b");
        ops.union(a, b);
        assertTrue(engine.matches(a, "a"));
        assertFalse(engine.matches(a, "b"));
    }

    // Complex intersections
    @Test void interComplex() {
        DFA r = ops.intersect(compile("(a|b)*"), compile("(b|c)*"));
        assertTrue(engine.matches(r, "bbb"));
        assertFalse(engine.matches(r, "a"));
    }

    // Chaining
    @Test void chainOperations() {
        DFA ab = ops.union(compile("a"), compile("b"));
        DFA notA = ops.difference(ab, compile("a"));
        assertTrue(engine.matches(notA, "b"));
        assertFalse(engine.matches(notA, "a"));
    }

    // Result is full DFA
    @Test void resultUsable() {
        DFA r = ops.intersect(compile("a|b"), compile("b|c"));
        DFA r2 = ops.union(r, compile("d"));
        assertTrue(engine.matches(r2, "b"));
        assertTrue(engine.matches(r2, "d"));
        assertFalse(engine.matches(r2, "a"));
    }

    // MakeComplete
    @Test void makeCompleteWorks() {
        DFA d = compile("a");
        DFA full = ops.makeComplete(d, d.getAlphabet());
        assertTrue(full.isComplete());
    }

    @Test void makeCompleteNotMutate() {
        DFA d = compile("a");
        ops.makeComplete(d, d.getAlphabet());
        assertEquals(2, d.getStates().size());
    }

    // Alphabet merging
    @Test void differentAlphabets() {
        DFA r = ops.intersect(compile("a"), compile("b"));
        assertFalse(engine.matches(r, "a"));
        assertFalse(engine.matches(r, "b"));
    }

    @Test void largerAlphabet() {
        DFA r = ops.union(compile("[abc]"), compile("[def]"));
        assertTrue(engine.matches(r, "a"));
        assertTrue(engine.matches(r, "f"));
        assertFalse(engine.matches(r, "g"));
    }
}