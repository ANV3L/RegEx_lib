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
