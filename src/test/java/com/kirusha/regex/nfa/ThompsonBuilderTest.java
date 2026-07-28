package com.kirusha.regex.nfa;

import com.kirusha.regex.lexer.Lexer;
import com.kirusha.regex.lexer.LexerResult;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ParserResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Полное тестирование ThompsonBuilder.
 *
 * Проверяем:
 * 1. Literal
 * 2. Epsilon
 * 3. Конкатенация
 * 4. Альтернация
 * 5. Kleene star
 * 6. Repeat {n}
 * 7. CharClass []
 * 8. Group ()
 * 9. BackReference (UnsupportedOperationException)
 * 10. Комбинированные выражения
 * 11. Ошибки
 */
@DisplayName("ThompsonBuilder")
class ThompsonBuilderTest {

    private Lexer lexer;
    private Parser parser;
    private ThompsonBuilder builder;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
        parser = new Parser();
        builder = new ThompsonBuilder();
    }

    private NFA buildNFA(String regex) {
        LexerResult lexerResult = lexer.tokenize(regex);
        ParserResult parserResult = parser.parse(lexerResult);
        return builder.build(parserResult);
    }

    // ==========================================
    // 1. LITERAL
    // ==========================================

    @Nested
    @DisplayName("1. Literal")
    class LiteralTests {

        @Test
        @DisplayName("'a' → NFA с 2 состояниями, 1 символ в алфавите")
        void singleLiteral() {
            NFA nfa = buildNFA("a");

            assertNotNull(nfa.getStartState());
            assertNotNull(nfa.getAcceptState());
            assertNotEquals(nfa.getStartState(), nfa.getAcceptState());
            assertEquals(2, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().contains("a"));
            assertEquals(1, nfa.getAlphabet().size());
        }

        @Test
        @DisplayName("'b' → алфавит содержит 'b'")
        void literalB() {
            NFA nfa = buildNFA("b");
            assertTrue(nfa.getAlphabet().contains("b"));
        }
    }

    // ==========================================
    // 2. EPSILON
    // ==========================================

    @Nested
    @DisplayName("2. Epsilon")
    class EpsilonTests {

        @Test
        @DisplayName("'~' → NFA с 2 состояниями, пустой алфавит")
        void singleEpsilon() {
            NFA nfa = buildNFA("~");

            assertEquals(2, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().isEmpty());
        }

        @Test
        @DisplayName("'~' → start имеет epsilon-переход в accept")
        void epsilonTransition() {
            NFA nfa = buildNFA("~");

            assertTrue(nfa.getStartState().getEpsilonTransitions().contains(nfa.getAcceptState()));
        }
    }

    // ==========================================
    // 3. КОНКАТЕНАЦИЯ
    // ==========================================

    @Nested
    @DisplayName("3. Конкатенация")
    class ConcatenationTests {

        @Test
        @DisplayName("'ab' → NFA с 4 состояниями, алфавит {a, b}")
        void simpleConcat() {
            NFA nfa = buildNFA("ab");

            assertEquals(4, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
            assertEquals(2, nfa.getAlphabet().size());
        }

        @Test
        @DisplayName("'abc' → NFA с 6 состояниями")
        void tripleConcat() {
            NFA nfa = buildNFA("abc");

            assertEquals(6, nfa.getStates().size());
            assertEquals(3, nfa.getAlphabet().size());
        }
    }

    // ==========================================
    // 4. АЛЬТЕРНАЦИЯ
    // ==========================================

    @Nested
    @DisplayName("4. Альтернация")
    class AlternationTests {

        @Test
        @DisplayName("'a|b' → NFA с 6 состояниями (2+2+2 новых)")
        void simpleAlternation() {
            NFA nfa = buildNFA("a|b");

            // 2 для 'a' + 2 для 'b' + 2 для обёртки = 6
            assertEquals(6, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
        }

        @Test
        @DisplayName("'a|b|c' → NFA с корректным алфавитом")
        void tripleAlternation() {
            NFA nfa = buildNFA("a|b|c");

            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
            assertTrue(nfa.getAlphabet().contains("c"));
        }

        @Test
        @DisplayName("'a|b' → start имеет два epsilon-перехода")
        void startHasTwoEpsilons() {
            NFA nfa = buildNFA("a|b");

            assertEquals(2, nfa.getStartState().getEpsilonTransitions().size());
        }
    }

    // ==========================================
    // 5. KLEENE STAR
    // ==========================================

    @Nested
    @DisplayName("5. Kleene star")
    class StarTests {

        @Test
        @DisplayName("'a*' → NFA с 4 состояниями (2 для a + 2 обёртки)")
        void singleStar() {
            NFA nfa = buildNFA("a*");

            assertEquals(4, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().contains("a"));
        }

        @Test
        @DisplayName("'a*' → start имеет epsilon в child.start и в accept")
        void starEpsilonFromStart() {
            NFA nfa = buildNFA("a*");

            assertEquals(2, nfa.getStartState().getEpsilonTransitions().size());
        }
    }

    // ==========================================
    // 6. REPEAT {n}
    // ==========================================

    @Nested
    @DisplayName("6. Repeat {n}")
    class RepeatTests {

        @Test
        @DisplayName("'a{3}' → NFA с 6 состояниями (3 копии по 2)")
        void repeatThree() {
            NFA nfa = buildNFA("a{3}");

            assertEquals(6, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().contains("a"));
        }

        @Test
        @DisplayName("'a{1}' → эквивалентно 'a'")
        void repeatOne() {
            NFA nfa = buildNFA("a{1}");

            assertEquals(2, nfa.getStates().size());
        }

        @Test
        @DisplayName("'a{0}' → эквивалентно epsilon")
        void repeatZero() {
            NFA nfa = buildNFA("a{0}");

            assertEquals(2, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().isEmpty());
        }
    }

    // ==========================================
    // 7. CHAR CLASS
    // ==========================================

    @Nested
    @DisplayName("7. CharClass")
    class CharClassTests {

        @Test
        @DisplayName("'[abc]' → NFA с 2 состояниями, алфавит {a, b, c}")
        void simpleCharClass() {
            NFA nfa = buildNFA("[abc]");

            assertEquals(2, nfa.getStates().size());
            assertEquals(3, nfa.getAlphabet().size());
            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
            assertTrue(nfa.getAlphabet().contains("c"));
        }

        @Test
        @DisplayName("'[]' → NFA с 2 состояниями, пустой алфавит (аналог #)")
        void emptyCharClass() {
            NFA nfa = buildNFA("[]");

            assertEquals(2, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().isEmpty());
        }

        @Test
        @DisplayName("'[ab]' → start имеет переходы по a и b")
        void charClassTransitions() {
            NFA nfa = buildNFA("[ab]");

            assertTrue(nfa.getStartState().getTransitions().containsKey("a"));
            assertTrue(nfa.getStartState().getTransitions().containsKey("b"));
        }
    }

    // ==========================================
    // 8. GROUP
    // ==========================================

    @Nested
    @DisplayName("8. Group")
    class GroupTests {

        @Test
        @DisplayName("'(a)' → NFA с 4 состояниями (2 для 'a' + 2 для границ группы)")
        void groupLiteral() {
            NFA nfa = buildNFA("(a)");

            assertEquals(4, nfa.getStates().size());
            assertTrue(nfa.getAlphabet().contains("a"));
        }

        @Test
        @DisplayName("'(a|b)' → NFA такой же, как для 'a|b'")
        void groupAlternation() {
            NFA nfa = buildNFA("(a|b)");

            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
        }

        @Test
        @DisplayName("groupCount сохраняется в NFA")
        void groupCountPreserved() {
            NFA nfa = buildNFA("(a)(b)");

            assertEquals(2, nfa.getGroupCount());
        }
    }

    // ==========================================
    // 9. BACKREFERENCE
    // ==========================================

    @Nested
    @DisplayName("9. BackReference")
    class BackReferenceTests {

        @Test
        @DisplayName("BackReference создает NFA с переходом по обратной ссылке")
        void backrefCreatesStates() {
            NFA nfa = buildNFA("(a)\\1");
            assertNotNull(nfa);
            assertTrue(nfa.getStates().size() > 0);
        }
    }

    // ==========================================
    // 10. КОМБИНИРОВАННЫЕ ВЫРАЖЕНИЯ
    // ==========================================

    @Nested
    @DisplayName("10. Комбинированные выражения")
    class CombinedTests {

        @Test
        @DisplayName("'(a|b)*c' → корректный NFA")
        void starThenConcat() {
            NFA nfa = buildNFA("(a|b)*c");

            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
            assertTrue(nfa.getAlphabet().contains("c"));
            assertNotNull(nfa.getStartState());
            assertNotNull(nfa.getAcceptState());
        }

        @Test
        @DisplayName("'[ab]{2}' → NFA для повторения char class")
        void charClassRepeat() {
            NFA nfa = buildNFA("[ab]{2}");

            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
            assertEquals(4, nfa.getStates().size());
        }

        @Test
        @DisplayName("'a*b*' → NFA для двух звёзд Клини")
        void doubleStars() {
            NFA nfa = buildNFA("a*b*");

            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
        }

        @Test
        @DisplayName("'(a|b)*c{2}[xy]~' → полное комбинированное выражение")
        void fullCombined() {
            NFA nfa = buildNFA("(a|b)*c{2}[xy]~");

            assertTrue(nfa.getAlphabet().contains("a"));
            assertTrue(nfa.getAlphabet().contains("b"));
            assertTrue(nfa.getAlphabet().contains("c"));
            assertTrue(nfa.getAlphabet().contains("x"));
            assertTrue(nfa.getAlphabet().contains("y"));
            assertNotNull(nfa.getStartState());
            assertNotNull(nfa.getAcceptState());
        }
    }

    // ==========================================
    // 11. ОШИБКИ
    // ==========================================

    @Nested
    @DisplayName("11. Ошибки")
    class ErrorTests {

        @Test
        @DisplayName("null ParserResult → IllegalArgumentException")
        void nullParserResult() {
            ThompsonBuilder builder = new ThompsonBuilder();
            assertThrows(IllegalArgumentException.class, () -> builder.build(null));
        }
    }
}
