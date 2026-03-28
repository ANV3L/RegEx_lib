package com.kirusha.regex.recovery;

import com.kirusha.regex.Regex;
import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAMinimizer;
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
 * Тестирование пакета Recovery.
 */
@DisplayName("Recovery")
class RecoveryTest {

    private Lexer lexer;
    private Parser parser;
    private ThompsonBuilder builder;
    private SubsetConstructor subsetConstructor;
    private DFAMinimizer minimizer;
    private StateEliminator eliminator;
    private DFAEngine engine;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
        parser = new Parser();
        builder = new ThompsonBuilder();
        subsetConstructor = new SubsetConstructor();
        minimizer = new DFAMinimizer();
        eliminator = new StateEliminator();
        engine = new DFAEngine();
    }

    private DFA compileMinDFA(String regex) {
        NFA nfa = builder.build(parser.parse(lexer.tokenize(regex)));
        DFA dfa = subsetConstructor.convert(nfa);
        return minimizer.minimize(dfa);
    }

    @Nested
    @DisplayName("StateElimination DFS tests")
    class EliminatorTests {

        @Test
        @DisplayName("Восстановление из простого 'a' возвращает 'a'")
        void testSimpleRecovery() {
            DFA minDFA = compileMinDFA("a");
            String recovered = eliminator.recover(minDFA);
            
            // Восстановленный regex может выглядеть как 'a' или нечто более замусоренное 
            // из-за отсутствия сложного упростителя, но он должен покрывать тот же язык.
            
            // Проверим, что восстановленный язык покрывает тот же матчинг
            Regex recoveredRegex = Regex.compile(recovered);
            assertTrue(recoveredRegex.matches("a"), "Восстановленный regex должен матчить 'a'");
            assertFalse(recoveredRegex.matches("b"));
        }

        @Test
        @DisplayName("Восстановление из 'ab' покрывает тот же язык")
        void testConcatRecovery() {
            DFA minDFA = compileMinDFA("ab");
            String recovered = eliminator.recover(minDFA);
            
            Regex r = Regex.compile(recovered);
            assertTrue(r.matches("ab"));
            assertFalse(r.matches("a"));
            assertFalse(r.matches("b"));
            assertFalse(r.matches("aba"));
        }

        @Test
        @DisplayName("Восстановление из 'a*' покрывает тот же язык")
        void testStarRecovery() {
            DFA minDFA = compileMinDFA("a*");
            String recovered = eliminator.recover(minDFA);
            
            Regex r = Regex.compile(recovered);
            assertTrue(r.matches(""));
            assertTrue(r.matches("a"));
            assertTrue(r.matches("aaa"));
            assertFalse(r.matches("b"));
            assertFalse(r.matches("aba"));
        }

        @Test
        @DisplayName("Восстановление из 'a|b' покрывает тот же язык")
        void testAlternationRecovery() {
            DFA minDFA = compileMinDFA("a|b");
            String recovered = eliminator.recover(minDFA);
            
            Regex r = Regex.compile(recovered);
            assertTrue(r.matches("a"));
            assertTrue(r.matches("b"));
            assertFalse(r.matches(""));
            assertFalse(r.matches("ab"));
        }
    }
}



@DisplayName("Recovery Stress Tests (40)")
class RecoveryStressTest {
    private StateEliminator elim;
    private Lexer lexer; private Parser parser; private ThompsonBuilder builder;
    private SubsetConstructor sub; private DFAMinimizer min;

    @BeforeEach void setUp() {
        elim = new StateEliminator();
        lexer = new Lexer(); parser = new Parser(); builder = new ThompsonBuilder();
        sub = new SubsetConstructor(); min = new DFAMinimizer();
    }

    private DFA compileDFA(String s) { return min.minimize(sub.convert(builder.build(parser.parse(lexer.tokenize(s))))); }

    private void verify(String regex, String[] accept, String[] reject) {
        String recovered = elim.recover(compileDFA(regex));
        assertNotNull(recovered);
        Regex r = Regex.compile(recovered);
        for (String s : accept) assertTrue(r.matches(s), "Should accept '" + s + "' for regex '" + regex + "', recovered: " + recovered);
        for (String s : reject) assertFalse(r.matches(s), "Should reject '" + s + "' for regex '" + regex + "', recovered: " + recovered);
    }

    @Test void literal_a() { verify("a", new String[]{"a"}, new String[]{"b", ""}); }
    @Test void literal_b() { verify("b", new String[]{"b"}, new String[]{"a"}); }
    @Test void concat_ab() { verify("ab", new String[]{"ab"}, new String[]{"a", "ba"}); }
    @Test void concat_abc() { verify("abc", new String[]{"abc"}, new String[]{"ab", "abcd"}); }
    @Test void alt_ab() { verify("a|b", new String[]{"a", "b"}, new String[]{"", "ab"}); }
    @Test void alt_abc() { verify("a|b|c", new String[]{"a", "c"}, new String[]{"d"}); }
    @Test void star_a() { verify("a*", new String[]{"", "a", "aaaa"}, new String[]{"b"}); }
    @Test void star_ab() { verify("(ab)*", new String[]{"", "ab", "abab"}, new String[]{"a"}); }
    @Test void charClass() { verify("[abc]", new String[]{"a", "c"}, new String[]{"d", ""}); }
    @Test void repeat2() { verify("a{2}", new String[]{"aa"}, new String[]{"a", "aaa"}); }
    @Test void repeat3() { verify("a{3}", new String[]{"aaa"}, new String[]{"aa", "aaaa"}); }
    @Test void starConcat() { verify("a*b", new String[]{"b", "ab", "aab"}, new String[]{"a", ""}); }
    @Test void altConcat() { verify("ab|cd", new String[]{"ab", "cd"}, new String[]{"ac", "bd"}); }
    @Test void groupStar() { verify("(a|b)*c", new String[]{"c", "ac", "bc", "ababc"}, new String[]{"a", ""}); }
    @Test void complex1() { verify("(a|b)*", new String[]{"", "a", "ab", "ba"}, new String[]{"c"}); }
    @Test void complex2() { verify("a*b*", new String[]{"", "aab", "bbb"}, new String[]{"ba"}); }
    @Test void charClassStar() { verify("[ab]*", new String[]{"", "ab", "ba"}, new String[]{"c"}); }
    @Test void singleChar() { verify("x", new String[]{"x"}, new String[]{"y", "xx"}); }
    @Test void twoAlts() { verify("a|bc", new String[]{"a", "bc"}, new String[]{"b", "ac"}); }
    @Test void starStar() { verify("a*b*", new String[]{"", "aabb"}, new String[]{"ba"}); }
    @Test void repeatOne() { verify("a{1}", new String[]{"a"}, new String[]{"", "aa"}); }
    @Test void emptyLang() { verify("#", new String[]{""}, new String[]{"a"}); }
    @Test void concatStar() { verify("ab*", new String[]{"a", "ab", "abbb"}, new String[]{"", "b"}); }
    @Test void altStar() { verify("(a|b)*c", new String[]{"c", "aac"}, new String[]{"ca"}); }
    @Test void multiCharClass() { verify("[abcde]", new String[]{"a", "e"}, new String[]{"f", ""}); }
    @Test void starCharClassConcat() { verify("[ab]*c", new String[]{"c", "abc"}, new String[]{"a", ""}); }
    @Test void doubleRepeat() { verify("a{2}b{2}", new String[]{"aabb"}, new String[]{"ab", "aab"}); }
    @Test void longLiteral() { verify("abcde", new String[]{"abcde"}, new String[]{"abcd", "abcdef"}); }
    @Test void altEpsilon() { verify("#|a", new String[]{"", "a"}, new String[]{"b"}); }
    @Test void notNull() { assertNotNull(elim.recover(compileDFA("a"))); }
    @Test void recoverReturnsString() { assertTrue(elim.recover(compileDFA("a")) instanceof String); }
    @Test void recoverComplex() { assertNotNull(elim.recover(compileDFA("(a|b)*c{2}[xy]"))); }
    @Test void recoverRepeat5() { verify("a{5}", new String[]{"aaaaa"}, new String[]{"aaaa"}); }
    @Test void recoverStarConcat() { verify("a*bc*", new String[]{"b", "ab", "abccc"}, new String[]{"", "ac"}); }
    @Test void recoverGroupAlt() { verify("(ab|cd)*", new String[]{"", "ab", "cd", "abcd"}, new String[]{"ac"}); }
    @Test void recoverDeep() { verify("((a|b)*c)*", new String[]{"", "c", "ababc"}, new String[]{"a"}); }
    @Test void recoverSingle() { verify("z", new String[]{"z"}, new String[]{"a"}); }
    @Test void recoverTwoChar() { verify("ab", new String[]{"ab"}, new String[]{"ba"}); }
    @Test void recoverThreeAlt() { verify("a|b|c|d", new String[]{"a", "d"}, new String[]{"e"}); }
    @Test void recoverEmpty() { verify("#", new String[]{""}, new String[]{"x"}); }
}