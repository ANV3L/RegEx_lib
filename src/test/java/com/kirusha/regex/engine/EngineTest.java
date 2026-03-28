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
