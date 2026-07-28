package com.kirusha.regex.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kirusha.regex.Regex;
import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.SubsetConstructor;
import com.kirusha.regex.lexer.Lexer;
import com.kirusha.regex.nfa.NFA;
import com.kirusha.regex.nfa.ThompsonBuilder;
import com.kirusha.regex.parser.Parser;

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
        String preprocessed = Regex.process(regex);
        return builder.build(parser.parse(lexer.tokenize(preprocessed)));
    }

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
        @DisplayName("'~' принимает только ''")
        void epsilonMatch() {
            DFA dfa = buildDFA("~");
            assertTrue(dfaEngine.matches(dfa, ""));
            assertFalse(dfaEngine.matches(dfa, "a"));
        }
    }

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

    @Nested
    @DisplayName("3. Практические кейсы групп захвата")
    class PracticalCaptureGroupTests {

        @Test
        @DisplayName("Простой IP-адрес (упрощённая версия)")
        void simpleIPPattern() {
            String pattern = "([0-9])([0-9])([0-9])\\.([0-9])([0-9])([0-9])\\.([0-9])([0-9])([0-9])\\.([0-9])([0-9])([0-9])";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "192.168.001.100", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("192.168.001.100", result.group(0));
            assertEquals("1", result.group(1));
            assertEquals("9", result.group(2));
            assertEquals("2", result.group(3));
        }

        @Test
        @DisplayName("Дата в формате DD-MM-YYYY")
        void datePattern() {
            String pattern = "([0-9])([0-9])-([0-9])([0-9])-([0-9])([0-9])([0-9])([0-9])";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "25-12-2024", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("25-12-2024", result.group(0));
            assertEquals("2", result.group(1));
            assertEquals("5", result.group(2));
        }

        @Test
        @DisplayName("Email-адрес (упрощённый с фиксированной длиной)")
        void emailPattern() {
            String pattern = "([a-z])([a-z])([a-z])([a-z])@([a-z])([a-z])([a-z])([a-z])\\.([a-z])([a-z])([a-z])";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "user@mail.com", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("user@mail.com", result.group(0));
            assertEquals("u", result.group(1));
            assertEquals("s", result.group(2));
        }

        @Test
        @DisplayName("URL с протоколом (фиксированной длины)")
        void urlPattern() {
            String pattern = "(http|https)://(a|b|c)(a|b|c)(a|b|c)\\.(c|d|e|o|m)(o|c|m)(m|c|o)";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "http://abc.com", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("http://abc.com", result.group(0));
            assertEquals("http", result.group(1));
        }

        @Test
        @DisplayName("Телефонный номер (упрощённый)")
        void phonePattern() {
            String pattern = "\\+([0-9]) \\(([0-9])([0-9])([0-9])\\) ([0-9])([0-9])([0-9])";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "+7 (123) 456", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("+7 (123) 456", result.group(0));
            assertEquals("7", result.group(1));
            assertEquals("1", result.group(2));
        }

        @Test
        @DisplayName("HTML-тег с содержимым (фиксированной длины)")
        void htmlTagPattern() {
            String pattern = "<(d|p|a)(i|b|n)(v|c|d)>(t|a|b|c)(e|n|d|x)(x|s|t|y)(t|s|p|u)</\\1\\2\\3>";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "<div>text</div>", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("<div>text</div>", result.group(0));
            assertEquals("d", result.group(1));
        }

        @Test
        @DisplayName("HTML-тег с несовпадающими тегами отклоняется")
        void htmlTagMismatch() {
            String pattern = "<(d|p)(i|a)(v|n)>(t|a)(e|b)(x|c)(t|d)</\\1\\2\\3>";
            
            NFA nfa = buildNFA(pattern);
            assertFalse(nfaEngine.matches(nfa, "<div>text</pan>"));
        }

        @Test
        @DisplayName("Повторяющееся слово (фиксированной длины)")
        void duplicateWordPattern() {
            String pattern = "([a-z])([a-z])([a-z]) \\1\\2\\3";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "cat cat", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("cat cat", result.group(0));
            assertEquals("c", result.group(1));
        }

        @Test
        @DisplayName("Повторяющееся слово с разными словами отклоняется")
        void duplicateWordMismatch() {
            String pattern = "([a-z])([a-z])([a-z]) \\1\\2\\3";
            NFA nfa = buildNFA(pattern);
            assertFalse(nfaEngine.matches(nfa, "cat dog"));
        }

        @Test
        @DisplayName("Версия ПО (major).(minor).(patch)")
        void versionPattern() {
            String pattern = "([0-9])\\.([0-9])\\.([0-9])";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "1.2.3", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("1.2.3", result.group(0));
            assertEquals("1", result.group(1));
            assertEquals("2", result.group(2));
            assertEquals("3", result.group(3));
        }

        @Test
        @DisplayName("Время в формате HH:MM:SS")
        void timePattern() {
            String pattern = "([0-9][0-9]|[0-9]):([0-9][0-9]|[0-9]):([0-9][0-9]|[0-9])";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "23:59:59", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("23:59:59", result.group(0));
            assertEquals("23", result.group(1));
            assertEquals("59", result.group(2));
            assertEquals("59", result.group(3));
            }

        @Test
        @DisplayName("Координаты (lat, lon) фиксированной длины")
        void coordinatesPattern() {
            String pattern = "([0-9])([0-9]),([0-9])([0-9])";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "55,37", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("55,37", result.group(0));
            assertEquals("5", result.group(1));
            assertEquals("5", result.group(2));
            assertEquals("3", result.group(3));
            assertEquals("7", result.group(4));
        }

        @Test
        @DisplayName("RGB цвет rgb(R,G,B) фиксированной длины")
        void rgbColorPattern() {
            String pattern = "rgb\\(([0-9])([0-9])([0-9]),([0-9])([0-9])([0-9]),([0-9])([0-9])([0-9])\\)";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "rgb(255,128,064)", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("rgb(255,128,064)", result.group(0));
            assertEquals("2", result.group(1));
            assertEquals("5", result.group(2));
            assertEquals("5", result.group(3));
            assertEquals("1", result.group(4));
            assertEquals("2", result.group(5));
            assertEquals("8", result.group(6));
            assertEquals("0", result.group(7));
            assertEquals("6", result.group(8));
            assertEquals("4", result.group(9));
        }

        @Test
        @DisplayName("Множественные вложенные группы")
        void multipleNestedGroups() {
            String pattern = "(((a)b)c)";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "abc", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("abc", result.group(0));
            assertEquals("abc", result.group(1));
            assertEquals("ab", result.group(2));
            assertEquals("a", result.group(3));
        }

        @Test
        @DisplayName("Группы с альтернацией и повторением")
        void groupsWithAlternationAndRepetition() {
            String pattern = "((a|b)(a|b)(a|b))c";
            
            NFA nfa = buildNFA(pattern);
            MatchResult result = nfaEngine.match(nfa, "abac", nfa.getGroupCount());
            
            assertTrue(result.matches());
            assertEquals("abac", result.group(0));
            assertEquals("aba", result.group(1));
        }
    }
}