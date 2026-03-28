package com.kirusha.regex.engine;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.SubsetConstructor;
import com.kirusha.regex.nfa.NFA;
import com.kirusha.regex.nfa.ThompsonBuilder;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ParserResult;
import com.kirusha.regex.lexer.Lexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование пакетов матчинга Engine.
 */
@DisplayName("Engine")
class EngineTest {

    private Lexer lexer;
    private Parser parser;
    private ThompsonBuilder builder;
    private SubsetConstructor subsetConstructor;
    private DFAEngine dfaEngine;
    private NFAEngine nfaEngine;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
        parser = new Parser();
        builder = new ThompsonBuilder();
        subsetConstructor = new SubsetConstructor();
        dfaEngine = new DFAEngine();
        nfaEngine = new NFAEngine();
    }

    private DFA buildDFA(String regex) {
        NFA nfa = buildNFA(regex);
        return subsetConstructor.convert(nfa);
    }

    private NFA buildNFA(String regex) {
        return builder.build(parser.parse(lexer.tokenize(regex)));
    }

    // ==========================================
    // 1. DFAEngine
    // ==========================================

    @Nested
    @DisplayName("1. DFAEngine")
    class DFAEngineTests {

        @Test
        @DisplayName("'a' принимает 'a' и отвергает 'b', ''")
        void literalMatch() {
            DFA dfa = buildDFA("a");
            assertTrue(dfaEngine.matches(dfa, "a"));
            assertFalse(dfaEngine.matches(dfa, "b"));
            assertFalse(dfaEngine.matches(dfa, ""));
            assertFalse(dfaEngine.matches(dfa, "aa"));
        }

        @Test
        @DisplayName("'a*' принимает '', 'a', 'aaa'")
        void starMatches() {
            DFA dfa = buildDFA("a*");
            assertTrue(dfaEngine.matches(dfa, ""));
            assertTrue(dfaEngine.matches(dfa, "a"));
            assertTrue(dfaEngine.matches(dfa, "aaa"));
            assertFalse(dfaEngine.matches(dfa, "b"));
            assertFalse(dfaEngine.matches(dfa, "ab"));
        }

        @Test
        @DisplayName("'a|b' принимает 'a' и 'b'")
        void alternationMatch() {
            DFA dfa = buildDFA("a|b");
            assertTrue(dfaEngine.matches(dfa, "a"));
            assertTrue(dfaEngine.matches(dfa, "b"));
            assertFalse(dfaEngine.matches(dfa, "c"));
        }

        @Test
        @DisplayName("'[abc]' принимает 'a', 'b', 'c'")
        void charClassMatch() {
            DFA dfa = buildDFA("[abc]");
            assertTrue(dfaEngine.matches(dfa, "a"));
            assertTrue(dfaEngine.matches(dfa, "b"));
            assertTrue(dfaEngine.matches(dfa, "c"));
            assertFalse(dfaEngine.matches(dfa, "d"));
        }

        @Test
        @DisplayName("'(a|b)*c' — комбинированное выражение")
        void combinedMatch() {
            DFA dfa = buildDFA("(a|b)*c");
            assertTrue(dfaEngine.matches(dfa, "c"));
            assertTrue(dfaEngine.matches(dfa, "ac"));
            assertTrue(dfaEngine.matches(dfa, "bc"));
            assertTrue(dfaEngine.matches(dfa, "abac"));
            assertFalse(dfaEngine.matches(dfa, "aba"));
            assertFalse(dfaEngine.matches(dfa, "cab"));
        }

        @Test
        @DisplayName("'#' принимает только ''")
        void epsilonMatch() {
            DFA dfa = buildDFA("#");
            assertTrue(dfaEngine.matches(dfa, ""));
            assertFalse(dfaEngine.matches(dfa, "a"));
        }
    }

    // ==========================================
    // 2. NFAEngine (с поддержкой групп)
    // ==========================================

    @Nested
    @DisplayName("2. NFAEngine")
    class NFAEngineTests {

        @Test
        @DisplayName("Матчинг обычного текста без групп")
        void simpleMatch() {
            NFA nfa = buildNFA("ab");
            MatchResult result = nfaEngine.match(nfa, "ab", nfa.getGroupCount());
            assertTrue(result.matches());
            assertEquals("ab", result.group(0));
            assertEquals(1, result.groupCount());
        }

        @Test
        @DisplayName("Матчинг с группами (DFS симуляция) — возврат результата")
        void dfsMatchWithGroups() {
            NFA nfa = buildNFA("(a)(b)");
            MatchResult result = nfaEngine.match(nfa, "ab", nfa.getGroupCount());
            assertTrue(result.matches());
            assertEquals("ab", result.group(0)); 
            assertEquals("a", result.group(1)); 
            assertEquals("b", result.group(2));
        }

        @Test
        @DisplayName("Вложенные группы захвата корректно обрабатываются")
        void nestedGroupsMatch() {
            NFA nfa = buildNFA("((a)b)");
            MatchResult result = nfaEngine.match(nfa, "ab", nfa.getGroupCount());
            assertTrue(result.matches());
            assertEquals("ab", result.group(0));
            assertEquals("ab", result.group(1));
            assertEquals("a", result.group(2));
        }

        @Test
        @DisplayName("Комплексное выражение '(a|b)*' матчится корректно")
        void repeatedMatch() {
            NFA nfa = buildNFA("(a|b)*");
            assertTrue(nfaEngine.matches(nfa, ""));
            assertTrue(nfaEngine.matches(nfa, "ababa"));
            assertFalse(nfaEngine.matches(nfa, "abac"));
        }
    }
}




@DisplayName("Engine Stress Tests (40)")
class EngineStressTest {
    private Lexer lexer; private Parser parser; private ThompsonBuilder builder;
    private SubsetConstructor sub; private DFAEngine dfa; private NFAEngine nfa;

    @BeforeEach void setUp() {
        lexer = new Lexer(); parser = new Parser(); builder = new ThompsonBuilder();
        sub = new SubsetConstructor(); dfa = new DFAEngine(); nfa = new NFAEngine();
    }

    private DFA buildDFA(String s) { return sub.convert(builder.build(parser.parse(lexer.tokenize(s)))); }
    private NFA buildNFA(String s) { return builder.build(parser.parse(lexer.tokenize(s))); }

    // DFA Engine
    @Test void dfaLiteral() { assertTrue(dfa.matches(buildDFA("a"), "a")); }
    @Test void dfaLiteralReject() { assertFalse(dfa.matches(buildDFA("a"), "b")); }
    @Test void dfaStar() { assertTrue(dfa.matches(buildDFA("a*"), "")); }
    @Test void dfaStar2() { assertTrue(dfa.matches(buildDFA("a*"), "aaa")); }
    @Test void dfaAlt() { assertTrue(dfa.matches(buildDFA("a|b"), "b")); }
    @Test void dfaConcat() { assertTrue(dfa.matches(buildDFA("ab"), "ab")); }
    @Test void dfaCharClass() { assertTrue(dfa.matches(buildDFA("[abc]"), "c")); }
    @Test void dfaEpsilon() { assertTrue(dfa.matches(buildDFA("#"), "")); }
    @Test void dfaRepeat() { assertTrue(dfa.matches(buildDFA("a{3}"), "aaa")); }
    @Test void dfaComplex() { assertTrue(dfa.matches(buildDFA("(a|b)*c"), "ababc")); }
    @Test void dfaMatchResult() { assertTrue(dfa.match(buildDFA("a"), "a").matches()); }
    @Test void dfaMatchResultGroup() { assertEquals("a", dfa.match(buildDFA("a"), "a").group(0)); }
    @Test void dfaNoMatch() { assertFalse(dfa.match(buildDFA("a"), "b").matches()); }
    @Test void dfaLong() { assertTrue(dfa.matches(buildDFA("a*b"), "aaaaab")); }
    @Test void dfaReject() { assertFalse(dfa.matches(buildDFA("a*b"), "aaaaa")); }

    // NFA Engine
    @Test void nfaSimple() { assertTrue(nfa.matches(buildNFA("ab"), "ab")); }
    @Test void nfaGroups() {
        NFA n = buildNFA("(a)(b)");
        MatchResult m = nfa.match(n, "ab", n.getGroupCount());
        assertEquals("a", m.group(1));
        assertEquals("b", m.group(2));
    }
    @Test void nfaNested() {
        NFA n = buildNFA("((a)b)");
        MatchResult m = nfa.match(n, "ab", n.getGroupCount());
        assertEquals("ab", m.group(1));
        assertEquals("a", m.group(2));
    }
    @Test void nfaStar() { assertTrue(nfa.matches(buildNFA("(a|b)*"), "abba")); }
    @Test void nfaStarEmpty() { assertTrue(nfa.matches(buildNFA("(a|b)*"), "")); }
    @Test void nfaStarReject() { assertFalse(nfa.matches(buildNFA("(a|b)*"), "abc")); }
    @Test void nfaGroupAlt() {
        NFA n = buildNFA("(a|b)c");
        assertEquals("b", nfa.match(n, "bc", n.getGroupCount()).group(1));
    }
    @Test void nfaThreeGroups() {
        NFA n = buildNFA("((a)(b))");
        MatchResult m = nfa.match(n, "ab", n.getGroupCount());
        assertEquals("ab", m.group(1));
        assertEquals("a", m.group(2));
        assertEquals("b", m.group(3));
    }
    @Test void nfaNoMatch() { assertFalse(nfa.matches(buildNFA("abc"), "abd")); }
    @Test void nfaMatchResult() { assertTrue(nfa.match(buildNFA("a"), "a", 0).matches()); }

    // MatchResult
    @Test void matchResultNoMatch() { assertFalse(MatchResult.noMatch().matches()); }
    @Test void matchResultGroupCount() { assertEquals(0, MatchResult.noMatch().groupCount()); }
    @Test void matchResultIterator() {
        MatchResult m = new MatchResult(true, java.util.List.of("full", "g1"));
        int c = 0; for (String s : m) c++;
        assertEquals(2, c);
    }
    @Test void matchResultGroup() {
        MatchResult m = new MatchResult(true, java.util.List.of("full", "g1"));
        assertEquals("g1", m.group(1));
    }
    @Test void matchResultOutOfBounds() {
        MatchResult m = new MatchResult(true, java.util.List.of("x"));
        assertThrows(IndexOutOfBoundsException.class, () -> m.group(5));
    }
    @Test void matchResultToString() { assertNotNull(MatchResult.noMatch().toString()); }
    @Test void nfaBackref() { assertTrue(nfa.matches(buildNFA("(a)\\1"), "aa")); }
    @Test void nfaBackrefReject() { assertFalse(nfa.matches(buildNFA("(a)\\1"), "ab")); }
    @Test void nfaBackrefMulti() { assertTrue(nfa.matches(buildNFA("(ab)\\1"), "abab")); }
    @Test void nfaGroupWithStar() { assertTrue(nfa.matches(buildNFA("(a)*b"), "aab")); }
}