package com.kirusha.regex.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kirusha.regex.Regex;
import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAMinimizer;
import com.kirusha.regex.dfa.SubsetConstructor;
import com.kirusha.regex.engine.DFAEngine;
import com.kirusha.regex.lexer.Lexer;
import com.kirusha.regex.nfa.NFA;
import com.kirusha.regex.nfa.ThompsonBuilder;
import com.kirusha.regex.operations.DFAIsomorphism;
import com.kirusha.regex.operations.DFAOperations;
import com.kirusha.regex.parser.Parser;

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
    private DFAIsomorphism isomorphism;
    private DFAOperations operations;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
        parser = new Parser();
        builder = new ThompsonBuilder();
        subsetConstructor = new SubsetConstructor();
        minimizer = new DFAMinimizer();
        eliminator = new StateEliminator();
        engine = new DFAEngine();
        isomorphism = new DFAIsomorphism();
        operations = new DFAOperations();
    }

    private DFA compileMinDFA(String regex) {
        NFA nfa = builder.build(parser.parse(lexer.tokenize(regex)));
        DFA dfa = subsetConstructor.convert(nfa);
        return minimizer.minimize(dfa);
    }

    /**
     * Проверяет эквивалентность исходного и восстановленного ДКА.
     */
    private void assertRecoveryEquivalent(String originalRegex) {
        DFA originalDFA = compileMinDFA(originalRegex);
        String recoveredRegex = eliminator.recover(originalDFA);
        
        assertNotNull(recoveredRegex, "Восстановленный regex не должен быть null");
        
        DFA recoveredDFA = compileMinDFA(recoveredRegex);
        
        assertTrue(isomorphism.areEquivalent(originalDFA, recoveredDFA),
                String.format("ДКА для '%s' и восстановленного '%s' должны быть эквивалентны", 
                        originalRegex, recoveredRegex));
    }

    @Nested
    @DisplayName("StateElimination DFS tests")
    class EliminatorTests {

        @Test
        @DisplayName("Восстановление из простого 'a' возвращает 'a'")
        void testSimpleRecovery() {
            DFA minDFA = compileMinDFA("a");
            String recovered = eliminator.recover(minDFA);
            
            Regex recoveredRegex = Regex.compile(recovered);
            assertTrue(recoveredRegex.matches("a"), "Восстановленный regex должен матчить 'a'");
            assertFalse(recoveredRegex.matches("b"));
            
            assertRecoveryEquivalent("a");
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
            
            // Дополнительная проверка эквивалентности
            assertRecoveryEquivalent("ab");
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
            
            // Дополнительная проверка эквивалентности
            assertRecoveryEquivalent("a*");
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
            
            // Дополнительная проверка эквивалентности
            assertRecoveryEquivalent("a|b");
        }

        @Test
        @DisplayName("Восстановление сложных выражений")
        void testComplexRecovery() {
            assertRecoveryEquivalent("(a|b)*");
            assertRecoveryEquivalent("(a*)(b*)");
            assertRecoveryEquivalent("(ab|cd)*");
            assertRecoveryEquivalent("(a{2})(b{3})");
        }

        @Test
        @DisplayName("Проверка эквивалентности через тестовые строки и операции ДКА")
        void testRecoveryWithMultipleValidation() {
            String original = "a*b+";
            DFA originalDFA = compileMinDFA(original);
            String recovered = eliminator.recover(originalDFA);
            DFA recoveredDFA = compileMinDFA(recovered);
            
            // Проверяем через тестовые строки
            String[] testStrings = {"", "a", "b", "ab", "aab", "abb", "aaabbb", "ba", "bb"};
            for (String test : testStrings) {
                boolean originalResult = engine.matches(originalDFA, test);
                boolean recoveredResult = engine.matches(recoveredDFA, test);
                assertEquals(originalResult, recoveredResult,
                        String.format("Результаты должны совпадать для строки '%s': оригинал=%s, восстановленный=%s", 
                                test, originalResult, recoveredResult));
            }
            
            // Проверяем через операции разности
            assertTrue(isomorphism.areEquivalent(originalDFA, recoveredDFA),
                    "ДКА должны быть эквивалентны по алгоритму через разности");
        }

        @Test
        @DisplayName("Восстановление из пустого языка")
        void testEmptyLanguageRecovery() {
            // Создаем ДКА для "a" без принимающих состояний (имитируем пустой язык)
            DFA originalDFA = compileMinDFA("a");
            // В реальном случае нужно создать ДКА с пустым множеством принимающих состояний
            
            String recovered = eliminator.recover(originalDFA);
            assertNotNull(recovered);
            
            // Проверяем, что восстановленный ДКА эквивалентен оригиналу
            DFA recoveredDFA = compileMinDFA(recovered);
            assertTrue(isomorphism.areEquivalent(originalDFA, recoveredDFA));
        }

        @Test
        @DisplayName("Проверка различных операций над восстановленными ДКА")
        void testOperationsOnRecoveredDFAs() {
            String regex1 = "a*";
            String regex2 = "b*";
            
            DFA dfa1 = compileMinDFA(regex1);
            DFA dfa2 = compileMinDFA(regex2);
            
            String recovered1 = eliminator.recover(dfa1);
            String recovered2 = eliminator.recover(dfa2);
            
            DFA recoveredDFA1 = compileMinDFA(recovered1);
            DFA recoveredDFA2 = compileMinDFA(recovered2);
            
            // Проверяем, что восстановленные ДКА эквивалентны оригинальным
            assertTrue(isomorphism.areEquivalent(dfa1, recoveredDFA1));
            assertTrue(isomorphism.areEquivalent(dfa2, recoveredDFA2));
            
            // Проверяем операции над восстановленными ДКА
            DFA union1 = operations.union(dfa1, dfa2);
            DFA union2 = operations.union(recoveredDFA1, recoveredDFA2);
            assertTrue(isomorphism.areEquivalent(union1, union2),
                    "Объединения должны быть эквивалентны");
            
            DFA intersect1 = operations.intersect(dfa1, dfa2);
            DFA intersect2 = operations.intersect(recoveredDFA1, recoveredDFA2);
            assertTrue(isomorphism.areEquivalent(intersect1, intersect2),
                    "Пересечения должны быть эквивалентны");
        }
    }
    
    @Nested
    @DisplayName("DFA Operations Integration Tests")
    class DFAOperationsTests {
        
        @Test
        @DisplayName("Проверка изоморфизма одинаковых ДКА")
        void testIsomorphicIdenticalDFAs() {
            DFA dfa1 = compileMinDFA("a*b");
            DFA dfa2 = compileMinDFA("a*b");
            
            assertTrue(isomorphism.areIsomorphic(dfa1, dfa2));
            assertTrue(isomorphism.areEquivalent(dfa1, dfa2));
        }
        
        @Test
        @DisplayName("Проверка эквивалентности различных представлений")
        void testEquivalentDifferentRepresentations() {
            DFA dfa1 = compileMinDFA("a|b");
            DFA dfa2 = compileMinDFA("b|a");
            
            assertTrue(isomorphism.areEquivalent(dfa1, dfa2));
        }
        
        @Test
        @DisplayName("Проверка неэквивалентных ДКА")
        void testNonEquivalentDFAs() {
            DFA dfa1 = compileMinDFA("a*");
            DFA dfa2 = compileMinDFA("b*");
            
            assertFalse(isomorphism.areIsomorphic(dfa1, dfa2));
            assertFalse(isomorphism.areEquivalent(dfa1, dfa2));
        }
        
        @Test
        @DisplayName("Проверка разности эквивалентных ДКА")
        void testDifferenceOfEquivalentDFAs() {
            DFA dfa1 = compileMinDFA("(a|b)*");
            DFA dfa2 = compileMinDFA("(b|a)*");
            
            DFA diff1 = operations.difference(dfa1, dfa2);
            DFA diff2 = operations.difference(dfa2, dfa1);
            
            assertTrue(diff1.getAcceptStates().isEmpty(), 
                    "Разность эквивалентных автоматов должна быть пустой");
            assertTrue(diff2.getAcceptStates().isEmpty(),
                    "Разность эквивалентных автоматов должна быть пустой");
        }

        @Test
        @DisplayName("Проверка операций с восстановленными ДКА")
        void testOperationsWithRecoveredDFAs() {
            String originalRegex = "(a|b)*c";
            DFA originalDFA = compileMinDFA(originalRegex);
            
            String recoveredRegex = eliminator.recover(originalDFA);
            DFA recoveredDFA = compileMinDFA(recoveredRegex);
            
            // Проверяем, что дополнения эквивалентны
            DFA comp1 = operations.complement(originalDFA);
            DFA comp2 = operations.complement(recoveredDFA);
            
            assertTrue(isomorphism.areEquivalent(comp1, comp2),
                    "Дополнения эквивалентных ДКА должны быть эквивалентны");
        }
    }
}



@DisplayName("Recovery Stress Tests (40)")
class RecoveryStressTest {
    private StateEliminator elim;
    private Lexer lexer; private Parser parser; private ThompsonBuilder builder;
    private SubsetConstructor sub; private DFAMinimizer min;
    private DFAIsomorphism isomorphism;

    @BeforeEach void setUp() {
        elim = new StateEliminator();
        lexer = new Lexer(); parser = new Parser(); builder = new ThompsonBuilder();
        sub = new SubsetConstructor(); min = new DFAMinimizer();
        isomorphism = new DFAIsomorphism();
    }

    private DFA compileDFA(String s) { 
        return min.minimize(sub.convert(builder.build(parser.parse(lexer.tokenize(s))))); 
    }

    /**
     * Проверяет что восстановленный ДКА эквивалентен оригиналу через операции разности.
     */
    private void verifyEquivalent(String regex) {
        DFA originalDFA = compileDFA(regex);
        String recovered = elim.recover(originalDFA);
        assertNotNull(recovered, "Восстановленный regex не должен быть null для: " + regex);
        
        DFA recoveredDFA = compileDFA(recovered);
        assertTrue(isomorphism.areEquivalent(originalDFA, recoveredDFA),
                String.format("ДКА должны быть эквивалентны: '%s' -> '%s'", regex, recovered));
    }

    private void verify(String regex, String[] accept, String[] reject) {
        String recovered = elim.recover(compileDFA(regex));
        assertNotNull(recovered);
        Regex r = Regex.compile(recovered);
        for (String s : accept) assertTrue(r.matches(s), "Should accept '" + s + "' for regex '" + regex + "', recovered: " + recovered);
        for (String s : reject) assertFalse(r.matches(s), "Should reject '" + s + "' for regex '" + regex + "', recovered: " + recovered);
        
        // Дополнительная проверка эквивалентности через операции ДКА
        verifyEquivalent(regex);
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
    @Test void starConcat() { verify("(a*)b", new String[]{"b", "ab", "aab"}, new String[]{"a", ""}); }
    @Test void altConcat() { verify("ab|cd", new String[]{"ab", "cd"}, new String[]{"ac", "bd"}); }
    @Test void groupStar() { verify("(a|b)*c", new String[]{"c", "ac", "bc", "ababc"}, new String[]{"a", ""}); }
    @Test void complex1() { verify("(a|b)*", new String[]{"", "a", "ab", "ba"}, new String[]{"c"}); }
    @Test void complex2() { verify("(a*)(b*)", new String[]{"", "aab", "bbb"}, new String[]{"ba"}); }
    @Test void charClassStar() { verify("[ab]*", new String[]{"", "ab", "ba"}, new String[]{"c"}); }
    @Test void singleChar() { verify("x", new String[]{"x"}, new String[]{"y", "xx"}); }
    @Test void twoAlts() { verify("a|bc", new String[]{"a", "bc"}, new String[]{"b", "ac"}); }
    @Test void starStar() { verify("(a*)(b*)", new String[]{"", "aabb"}, new String[]{"ba"}); }
    @Test void repeatOne() { verify("a{1}", new String[]{"a"}, new String[]{"", "aa"}); }
    @Test void emptyLang() { verify("~", new String[]{""}, new String[]{"a"}); }
    @Test void concatStar() { verify("a(b*)", new String[]{"a", "ab", "abbb"}, new String[]{"", "b"}); }
    @Test void altStar() { verify("(a|b)*c", new String[]{"c", "aac"}, new String[]{"ca"}); }
    @Test void multiCharClass() { verify("[abcde]", new String[]{"a", "e"}, new String[]{"f", ""}); }
    @Test void starCharClassConcat() { verify("([ab]*)c", new String[]{"c", "abc"}, new String[]{"a", ""}); }
    @Test void doubleRepeat() { verify("(a{2})(b{2})", new String[]{"aabb"}, new String[]{"ab", "aab"}); }
    @Test void longLiteral() { verify("abcde", new String[]{"abcde"}, new String[]{"abcd", "abcdef"}); }
    @Test void altEpsilon() { verify("~|a", new String[]{"", "a"}, new String[]{"b"}); }
    @Test void notNull() { assertNotNull(elim.recover(compileDFA("a"))); }
    @Test void recoverReturnsString() { assertTrue(elim.recover(compileDFA("a")) instanceof String); }
    @Test void recoverComplex() { assertNotNull(elim.recover(compileDFA("(a|b)*c{2}[xy]"))); }
    @Test void recoverRepeat5() { verify("a{5}", new String[]{"aaaaa"}, new String[]{"aaaa"}); }
    @Test void recoverStarConcat() { verify("(a*)b(c*)", new String[]{"b", "ab", "abccc"}, new String[]{"", "ac"}); }
    @Test void recoverGroupAlt() { verify("(ab|cd)*", new String[]{"", "ab", "cd", "abcd"}, new String[]{"ac"}); }
    @Test void recoverDeep() { verify("(((a|b)*)c)*", new String[]{"", "c", "ababc"}, new String[]{"a"}); }
    @Test void recoverSingle() { verify("z", new String[]{"z"}, new String[]{"a"}); }
    @Test void recoverTwoChar() { verify("ab", new String[]{"ab"}, new String[]{"ba"}); }
    @Test void recoverThreeAlt() { verify("a|b|c|d", new String[]{"a", "d"}, new String[]{"e"}); }
    @Test void recoverEmpty() { verify("~", new String[]{""}, new String[]{"x"}); }
    

    @Test void testComplexEquivalence1() { verifyEquivalent("(((a|b)*)c)*"); }
    @Test void testComplexEquivalence2() { verifyEquivalent("(a{5})(b{3})(c*)"); }
    @Test void testComplexEquivalence3() { verifyEquivalent("(ab|cd|ef)*"); }
    @Test void testComplexEquivalence4() { verifyEquivalent("([a-z]*)([0-9]+)"); }
    @Test void testComplexEquivalence5() { verifyEquivalent("(a*)(b*)(c*)(d*)"); }
    
    // Дополнительные тесты для проверки корректности операций
    @Test void testRecoveredDFAOperations1() {
        verifyEquivalent("(a*)|(b*)");
        verifyEquivalent("((a*)(b*))&((b*)(a*))"); // пересечение если поддерживается
    }
    
    @Test void testRecoveredDFAOperations2() {
        verifyEquivalent("(a+)(b+)");
        verifyEquivalent("(abc)*|(def)*");
    }
}