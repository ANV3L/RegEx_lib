package com.kirusha.regex.lexer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Полное тестирование лексера.
 *
 * Структура тестов:
 * 1. Пустой и минимальный ввод
 * 2. Обычные символы (CHAR)
 * 3. Каждый метасимвол отдельно
 * 4. Экранирование (\)
 * 5. Обратные ссылки (\1..\9)
 * 6. Числа (NUMBER)
 * 7. Символ эпсилон (#)
 * 8. Комбинированные выражения
 * 9. Граничные случаи и ошибки
 * 10. Позиции токенов
 * 11. LexerResult
 */
@DisplayName("Lexer")
class LexerTest {

    private Lexer lexer;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
    }

    // ==========================================
    // 1. ПУСТОЙ И МИНИМАЛЬНЫЙ ВВОД
    // ==========================================

    @Nested
    @DisplayName("1. Пустой и минимальный ввод")
    class EmptyAndMinimalInput {

        @Test
        @DisplayName("Пустая строка → пустой список токенов")
        void emptyString() {
            LexerResult result = lexer.tokenize("");
            assertTrue(result.getTokens().isEmpty());
            assertTrue(result.isEmpty());
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("Один обычный символ 'a'")
        void singleChar() {
            LexerResult result = lexer.tokenize("a");
            assertEquals(1, result.size());
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("a", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("Один метасимвол '*'")
        void singleMeta() {
            LexerResult result = lexer.tokenize("*");
            assertEquals(1, result.size());
            assertEquals(TokenType.STAR, result.getTokens().get(0).getType());
        }
    }

    // ==========================================
    // 2. ОБЫЧНЫЕ СИМВОЛЫ (CHAR)
    // ==========================================

    @Nested
    @DisplayName("2. Обычные символы")
    class RegularChars {

        @Test
        @DisplayName("Строка из букв 'abc' → три CHAR")
        void multipleChars() {
            LexerResult result = lexer.tokenize("abc");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            assertEquals(new Token(TokenType.CHAR, "a", 0), tokens.get(0));
            assertEquals(new Token(TokenType.CHAR, "b", 1), tokens.get(1));
            assertEquals(new Token(TokenType.CHAR, "c", 2), tokens.get(2));
        }

        @Test
        @DisplayName("Пробел является обычным печатным символом")
        void spaceIsChar() {
            LexerResult result = lexer.tokenize(" ");
            assertEquals(1, result.size());
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals(" ", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("Заглавные буквы — обычные символы")
        void upperCaseChars() {
            LexerResult result = lexer.tokenize("ABC");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            tokens.forEach(t -> assertEquals(TokenType.CHAR, t.getType()));
        }

        @Test
        @DisplayName("Символы Unicode (кириллица) — обычные символы")
        void cyrillicChars() {
            LexerResult result = lexer.tokenize("абв");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            assertEquals("а", tokens.get(0).getValue());
            assertEquals("б", tokens.get(1).getValue());
            assertEquals("в", tokens.get(2).getValue());
        }

        @Test
        @DisplayName("Знаки препинания (не метасимволы) — обычные символы")
        void punctuationChars() {
            LexerResult result = lexer.tokenize("!@$%^&-+=:;,.<>?/~`");
            List<Token> tokens = result.getTokens();

            tokens.forEach(t -> assertEquals(TokenType.CHAR, t.getType()));
        }
    }

    // ==========================================
    // 3. КАЖДЫЙ МЕТАСИМВОЛ ОТДЕЛЬНО
    // ==========================================

    @Nested
    @DisplayName("3. Метасимволы по отдельности")
    class MetaCharacters {

        @Test
        @DisplayName("'|' → PIPE")
        void pipe() {
            LexerResult result = lexer.tokenize("|");
            assertEquals(TokenType.PIPE, result.getTokens().get(0).getType());
            assertEquals("|", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'*' → STAR")
        void star() {
            LexerResult result = lexer.tokenize("*");
            assertEquals(TokenType.STAR, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("'(' → LPAREN")
        void lparen() {
            LexerResult result = lexer.tokenize("(");
            assertEquals(TokenType.LPAREN, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("')' → RPAREN")
        void rparen() {
            LexerResult result = lexer.tokenize(")");
            assertEquals(TokenType.RPAREN, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("'[' → LBRACKET")
        void lbracket() {
            LexerResult result = lexer.tokenize("[");
            assertEquals(TokenType.LBRACKET, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("']' → RBRACKET")
        void rbracket() {
            LexerResult result = lexer.tokenize("]");
            assertEquals(TokenType.RBRACKET, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("'{' → LBRACE")
        void lbrace() {
            LexerResult result = lexer.tokenize("{");
            assertEquals(TokenType.LBRACE, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("'}' → RBRACE")
        void rbrace() {
            LexerResult result = lexer.tokenize("}");
            assertEquals(TokenType.RBRACE, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("'#' → EPSILON")
        void epsilon() {
            LexerResult result = lexer.tokenize("#");
            assertEquals(TokenType.EPSILON, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("Все метасимволы подряд '|*()[]{}#'")
        void allMetaChars() {
            LexerResult result = lexer.tokenize("|*()[]{}#");
            List<Token> tokens = result.getTokens();

            assertEquals(9, tokens.size());
            assertEquals(TokenType.PIPE, tokens.get(0).getType());
            assertEquals(TokenType.STAR, tokens.get(1).getType());
            assertEquals(TokenType.LPAREN, tokens.get(2).getType());
            assertEquals(TokenType.RPAREN, tokens.get(3).getType());
            assertEquals(TokenType.LBRACKET, tokens.get(4).getType());
            assertEquals(TokenType.RBRACKET, tokens.get(5).getType());
            assertEquals(TokenType.LBRACE, tokens.get(6).getType());
            assertEquals(TokenType.RBRACE, tokens.get(7).getType());
            assertEquals(TokenType.EPSILON, tokens.get(8).getType());
        }
    }

    // ==========================================
    // 4. ЭКРАНИРОВАНИЕ
    // ==========================================

    @Nested
    @DisplayName("4. Экранирование метасимволов")
    class Escaping {

        @Test
        @DisplayName("'\\*' → CHAR('*') — экранированная звезда")
        void escapedStar() {
            LexerResult result = lexer.tokenize("\\*");
            List<Token> tokens = result.getTokens();

            assertEquals(1, tokens.size());
            assertEquals(TokenType.CHAR, tokens.get(0).getType());
            assertEquals("*", tokens.get(0).getValue());
        }

        @Test
        @DisplayName("'\\|' → CHAR('|') — экранированный пайп")
        void escapedPipe() {
            LexerResult result = lexer.tokenize("\\|");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("|", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\(' → CHAR('(') — экранированная скобка")
        void escapedLparen() {
            LexerResult result = lexer.tokenize("\\(");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("(", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\)' → CHAR(')') — экранированная скобка")
        void escapedRparen() {
            LexerResult result = lexer.tokenize("\\)");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals(")", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\[' → CHAR('[') — экранированная скобка")
        void escapedLbracket() {
            LexerResult result = lexer.tokenize("\\[");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("[", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\]' → CHAR(']') — экранированная скобка")
        void escapedRbracket() {
            LexerResult result = lexer.tokenize("\\]");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("]", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\{' → CHAR('{') — экранированная скобка")
        void escapedLbrace() {
            LexerResult result = lexer.tokenize("\\{");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("{", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\}' → CHAR('}') — экранированная скобка")
        void escapedRbrace() {
            LexerResult result = lexer.tokenize("\\}");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("}", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\#' → CHAR('#') — экранированный эпсилон")
        void escapedHash() {
            LexerResult result = lexer.tokenize("\\#");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("#", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\\\' → CHAR('\\') — экранированный бэкслеш")
        void escapedBackslash() {
            LexerResult result = lexer.tokenize("\\\\");
            List<Token> tokens = result.getTokens();

            assertEquals(1, tokens.size());
            assertEquals(TokenType.CHAR, tokens.get(0).getType());
            assertEquals("\\", tokens.get(0).getValue());
        }

        @Test
        @DisplayName("'\\a' → CHAR('a') — экранирование обычного символа")
        void escapedRegularChar() {
            LexerResult result = lexer.tokenize("\\a");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("a", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'a\\*b' → CHAR(a), CHAR(*), CHAR(b)")
        void escapedInMiddle() {
            LexerResult result = lexer.tokenize("a\\*b");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            assertEquals(new Token(TokenType.CHAR, "a", 0), tokens.get(0));
            assertEquals(new Token(TokenType.CHAR, "*", 1), tokens.get(1));
            assertEquals(new Token(TokenType.CHAR, "b", 3), tokens.get(2));
        }

        @Test
        @DisplayName("Множественное экранирование '\\*\\|\\#'")
        void multipleEscapes() {
            LexerResult result = lexer.tokenize("\\*\\|\\#");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            assertEquals(TokenType.CHAR, tokens.get(0).getType());
            assertEquals("*", tokens.get(0).getValue());
            assertEquals(TokenType.CHAR, tokens.get(1).getType());
            assertEquals("|", tokens.get(1).getValue());
            assertEquals(TokenType.CHAR, tokens.get(2).getType());
            assertEquals("#", tokens.get(2).getValue());
        }
    }

    // ==========================================
    // 5. ОБРАТНЫЕ ССЫЛКИ (BACKREF)
    // ==========================================

    @Nested
    @DisplayName("5. Обратные ссылки")
    class BackReferences {

        @Test
        @DisplayName("'\\1' → BACKREF('1')")
        void backref1() {
            LexerResult result = lexer.tokenize("\\1");
            List<Token> tokens = result.getTokens();

            assertEquals(1, tokens.size());
            assertEquals(TokenType.BACKREF, tokens.get(0).getType());
            assertEquals("1", tokens.get(0).getValue());
        }

        @Test
        @DisplayName("'\\9' → BACKREF('9')")
        void backref9() {
            LexerResult result = lexer.tokenize("\\9");
            assertEquals(TokenType.BACKREF, result.getTokens().get(0).getType());
            assertEquals("9", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'\\0' — ноль НЕ является ссылкой → CHAR('0')")
        void backref0IsChar() {
            LexerResult result = lexer.tokenize("\\0");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("0", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("Все ссылки от \\1 до \\9")
        void allBackrefs() {
            for (int i = 1; i <= 9; i++) {
                LexerResult result = lexer.tokenize("\\" + i);
                assertEquals(TokenType.BACKREF, result.getTokens().get(0).getType());
                assertEquals(String.valueOf(i), result.getTokens().get(0).getValue());
            }
        }

        @Test
        @DisplayName("'(a)\\1' → LPAREN, CHAR(a), RPAREN, BACKREF(1)")
        void backrefInContext() {
            LexerResult result = lexer.tokenize("(a)\\1");
            List<Token> tokens = result.getTokens();

            assertEquals(4, tokens.size());
            assertEquals(TokenType.LPAREN, tokens.get(0).getType());
            assertEquals(TokenType.CHAR, tokens.get(1).getType());
            assertEquals(TokenType.RPAREN, tokens.get(2).getType());
            assertEquals(TokenType.BACKREF, tokens.get(3).getType());
            assertEquals("1", tokens.get(3).getValue());
        }
    }

    // ==========================================
    // 6. ЧИСЛА (NUMBER)
    // ==========================================

    @Nested
    @DisplayName("6. Числа")
    class Numbers {

        @Test
        @DisplayName("Одиночная цифра '3' → NUMBER('3')")
        void singleDigit() {
            LexerResult result = lexer.tokenize("3");
            assertEquals(TokenType.NUMBER, result.getTokens().get(0).getType());
            assertEquals("3", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("Многозначное число '123' → NUMBER('123')")
        void multiDigitNumber() {
            LexerResult result = lexer.tokenize("123");
            List<Token> tokens = result.getTokens();

            assertEquals(1, tokens.size());
            assertEquals(TokenType.NUMBER, tokens.get(0).getType());
            assertEquals("123", tokens.get(0).getValue());
        }

        @Test
        @DisplayName("Число ноль '0' → NUMBER('0')")
        void zero() {
            LexerResult result = lexer.tokenize("0");
            assertEquals(TokenType.NUMBER, result.getTokens().get(0).getType());
            assertEquals("0", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'{3}' → LBRACE, NUMBER(3), RBRACE")
        void numberInBraces() {
            LexerResult result = lexer.tokenize("{3}");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            assertEquals(TokenType.LBRACE, tokens.get(0).getType());
            assertEquals(TokenType.NUMBER, tokens.get(1).getType());
            assertEquals("3", tokens.get(1).getValue());
            assertEquals(TokenType.RBRACE, tokens.get(2).getType());
        }

        @Test
        @DisplayName("'{12}' → LBRACE, NUMBER(12), RBRACE — многозначное число")
        void multiDigitInBraces() {
            LexerResult result = lexer.tokenize("{12}");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            assertEquals(TokenType.NUMBER, tokens.get(1).getType());
            assertEquals("12", tokens.get(1).getValue());
        }

        @Test
        @DisplayName("'a{5}b' → CHAR(a), LBRACE, NUMBER(5), RBRACE, CHAR(b)")
        void numberInContext() {
            LexerResult result = lexer.tokenize("a{5}b");
            List<Token> tokens = result.getTokens();

            assertEquals(5, tokens.size());
            assertEquals(TokenType.CHAR, tokens.get(0).getType());
            assertEquals(TokenType.LBRACE, tokens.get(1).getType());
            assertEquals(TokenType.NUMBER, tokens.get(2).getType());
            assertEquals("5", tokens.get(2).getValue());
            assertEquals(TokenType.RBRACE, tokens.get(3).getType());
            assertEquals(TokenType.CHAR, tokens.get(4).getType());
        }
    }

    // ==========================================
    // 7. ЭПСИЛОН (#)
    // ==========================================

    @Nested
    @DisplayName("7. Эпсилон")
    class Epsilon {

        @Test
        @DisplayName("'#' → EPSILON")
        void singleEpsilon() {
            LexerResult result = lexer.tokenize("#");
            assertEquals(TokenType.EPSILON, result.getTokens().get(0).getType());
        }

        @Test
        @DisplayName("'a#b' → CHAR(a), EPSILON, CHAR(b)")
        void epsilonBetweenChars() {
            LexerResult result = lexer.tokenize("a#b");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            assertEquals(TokenType.CHAR, tokens.get(0).getType());
            assertEquals(TokenType.EPSILON, tokens.get(1).getType());
            assertEquals(TokenType.CHAR, tokens.get(2).getType());
        }

        @Test
        @DisplayName("'\\#' → CHAR('#') — экранированный эпсилон это обычный символ")
        void escapedEpsilonIsChar() {
            LexerResult result = lexer.tokenize("\\#");
            assertEquals(TokenType.CHAR, result.getTokens().get(0).getType());
            assertEquals("#", result.getTokens().get(0).getValue());
        }

        @Test
        @DisplayName("'##' → EPSILON, EPSILON")
        void doubleEpsilon() {
            LexerResult result = lexer.tokenize("##");
            List<Token> tokens = result.getTokens();

            assertEquals(2, tokens.size());
            assertEquals(TokenType.EPSILON, tokens.get(0).getType());
            assertEquals(TokenType.EPSILON, tokens.get(1).getType());
        }
    }

    // ==========================================
    // 8. КОМБИНИРОВАННЫЕ ВЫРАЖЕНИЯ
    // ==========================================

    @Nested
    @DisplayName("8. Комбинированные выражения")
    class CombinedExpressions {

        @Test
        @DisplayName("'(a|b)*' → LPAREN, CHAR(a), PIPE, CHAR(b), RPAREN, STAR")
        void simpleAlternation() {
            LexerResult result = lexer.tokenize("(a|b)*");
            List<Token> tokens = result.getTokens();

            assertEquals(6, tokens.size());
            assertEquals(TokenType.LPAREN, tokens.get(0).getType());
            assertEquals(TokenType.CHAR, tokens.get(1).getType());
            assertEquals(TokenType.PIPE, tokens.get(2).getType());
            assertEquals(TokenType.CHAR, tokens.get(3).getType());
            assertEquals(TokenType.RPAREN, tokens.get(4).getType());
            assertEquals(TokenType.STAR, tokens.get(5).getType());
        }

        @Test
        @DisplayName("'[abc]' → LBRACKET, CHAR(a), CHAR(b), CHAR(c), RBRACKET")
        void charClass() {
            LexerResult result = lexer.tokenize("[abc]");
            List<Token> tokens = result.getTokens();

            assertEquals(5, tokens.size());
            assertEquals(TokenType.LBRACKET, tokens.get(0).getType());
            assertEquals(TokenType.CHAR, tokens.get(1).getType());
            assertEquals(TokenType.CHAR, tokens.get(2).getType());
            assertEquals(TokenType.CHAR, tokens.get(3).getType());
            assertEquals(TokenType.RBRACKET, tokens.get(4).getType());
        }

        @Test
        @DisplayName("'[]' — пустой набор → LBRACKET, RBRACKET")
        void emptyCharClass() {
            LexerResult result = lexer.tokenize("[]");
            List<Token> tokens = result.getTokens();

            assertEquals(2, tokens.size());
            assertEquals(TokenType.LBRACKET, tokens.get(0).getType());
            assertEquals(TokenType.RBRACKET, tokens.get(1).getType());
        }

        @Test
        @DisplayName("'a{3}' → CHAR(a), LBRACE, NUMBER(3), RBRACE")
        void repeatExpression() {
            LexerResult result = lexer.tokenize("a{3}");
            List<Token> tokens = result.getTokens();

            assertEquals(4, tokens.size());
            assertEquals(TokenType.CHAR, tokens.get(0).getType());
            assertEquals(TokenType.LBRACE, tokens.get(1).getType());
            assertEquals(TokenType.NUMBER, tokens.get(2).getType());
            assertEquals(TokenType.RBRACE, tokens.get(3).getType());
        }

        @Test
        @DisplayName("'(a|b)*c{2}[xy]#' — полное комбинированное выражение")
        void fullCombined() {
        LexerResult result = lexer.tokenize("(a|b)*c{2}[xy]#");
        List<Token> tokens = result.getTokens();

        assertEquals(15, tokens.size());

        assertEquals(TokenType.LPAREN,   tokens.get(0).getType());   // (
        assertEquals(TokenType.CHAR,     tokens.get(1).getType());   // a
        assertEquals(TokenType.PIPE,     tokens.get(2).getType());   // |
        assertEquals(TokenType.CHAR,     tokens.get(3).getType());   // b
        assertEquals(TokenType.RPAREN,   tokens.get(4).getType());   // )
        assertEquals(TokenType.STAR,     tokens.get(5).getType());   // *
        assertEquals(TokenType.CHAR,     tokens.get(6).getType());   // c
        assertEquals(TokenType.LBRACE,   tokens.get(7).getType());   // {
        assertEquals(TokenType.NUMBER,   tokens.get(8).getType());   // 2
        assertEquals(TokenType.RBRACE,   tokens.get(9).getType());   // }
        assertEquals(TokenType.LBRACKET, tokens.get(10).getType());  // [
        assertEquals(TokenType.CHAR,     tokens.get(11).getType());  // x
        assertEquals(TokenType.CHAR,     tokens.get(12).getType());  // y
        assertEquals(TokenType.RBRACKET, tokens.get(13).getType());  // ]
        assertEquals(TokenType.EPSILON,  tokens.get(14).getType());  // #
        }

        @Test
        @DisplayName("'((a)(b))\\1' — вложенные группы с backref")
        void nestedGroupsWithBackref() {
            LexerResult result = lexer.tokenize("((a)(b))\\1");
            List<Token> tokens = result.getTokens();

            assertEquals(9, tokens.size());

            assertEquals(TokenType.LPAREN,  tokens.get(0).getType());  // (
            assertEquals(TokenType.LPAREN,  tokens.get(1).getType());  // (
            assertEquals(TokenType.CHAR,    tokens.get(2).getType());  // a
            assertEquals(TokenType.RPAREN,  tokens.get(3).getType());  // )
            assertEquals(TokenType.LPAREN,  tokens.get(4).getType());  // (
            assertEquals(TokenType.CHAR,    tokens.get(5).getType());  // b
            assertEquals(TokenType.RPAREN,  tokens.get(6).getType());  // )
            assertEquals(TokenType.RPAREN,  tokens.get(7).getType());  // )
            assertEquals(TokenType.BACKREF, tokens.get(8).getType());  // \1
            assertEquals("1", tokens.get(8).getValue());
        }

        @Test
        @DisplayName("'[\\*\\|]' — экранирование внутри набора символов")
        void escapedInsideCharClass() {
            LexerResult result = lexer.tokenize("[\\*\\|]");
            List<Token> tokens = result.getTokens();

            // [ \* \| ]
            assertEquals(4, tokens.size());
            assertEquals(TokenType.LBRACKET, tokens.get(0).getType());
            assertEquals(TokenType.CHAR, tokens.get(1).getType());
            assertEquals("*", tokens.get(1).getValue());
            assertEquals(TokenType.CHAR, tokens.get(2).getType());
            assertEquals("|", tokens.get(2).getValue());
            assertEquals(TokenType.RBRACKET, tokens.get(3).getType());
        }

        @Test
        @DisplayName("'a**' → CHAR(a), STAR, STAR — лексер не проверяет грамматику")
        void doubleStarIsLexerJob() {
            LexerResult result = lexer.tokenize("a**");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            assertEquals(TokenType.CHAR, tokens.get(0).getType());
            assertEquals(TokenType.STAR, tokens.get(1).getType());
            assertEquals(TokenType.STAR, tokens.get(2).getType());
        }

        @Test
        @DisplayName("'|||' → PIPE, PIPE, PIPE — лексер не проверяет грамматику")
        void multiplePipes() {
            LexerResult result = lexer.tokenize("|||");
            List<Token> tokens = result.getTokens();

            assertEquals(3, tokens.size());
            tokens.forEach(t -> assertEquals(TokenType.PIPE, t.getType()));
        }
    }

    // ==========================================
    // 9. ГРАНИЧНЫЕ СЛУЧАИ И ОШИБКИ
    // ==========================================

    @Nested
    @DisplayName("9. Граничные случаи и ошибки")
    class EdgeCasesAndErrors {

        @Test
        @DisplayName("'\\' в конце строки → LexerException")
        void backslashAtEnd() {
            assertThrows(LexerException.class, () -> lexer.tokenize("\\"));
        }

        @Test
        @DisplayName("'abc\\' — бэкслеш в конце → LexerException")
        void backslashAtEndAfterChars() {
            LexerException ex = assertThrows(LexerException.class,
                    () -> lexer.tokenize("abc\\"));
            assertEquals(3, ex.getPosition());
        }

        @Test
        @DisplayName("'a\\' — бэкслеш в конце → LexerException с позицией 1")
        void backslashAtEndPosition() {
            LexerException ex = assertThrows(LexerException.class,
                    () -> lexer.tokenize("a\\"));
            assertEquals(1, ex.getPosition());
        }

        @Test
        @DisplayName("null вход → NullPointerException или IllegalArgumentException")
        void nullInput() {
            assertThrows(Exception.class, () -> lexer.tokenize(null));
        }

        @Test
        @DisplayName("Очень длинная строка — лексер не падает")
        void veryLongInput() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append("a");
            }
            LexerResult result = lexer.tokenize(sb.toString());
            assertEquals(10000, result.size());
        }

        @Test
        @DisplayName("Строка только из бэкслешей '\\\\\\\\' → два CHAR('\\')")
        void onlyBackslashes() {
            LexerResult result = lexer.tokenize("\\\\\\\\");
            List<Token> tokens = result.getTokens();

            assertEquals(2, tokens.size());
            tokens.forEach(t -> {
                assertEquals(TokenType.CHAR, t.getType());
                assertEquals("\\", t.getValue());
            });
        }
    }

    // ==========================================
    // 10. ПОЗИЦИИ ТОКЕНОВ
    // ==========================================

    @Nested
    @DisplayName("10. Позиции токенов")
    class TokenPositions {

        @Test
        @DisplayName("'abc' — позиции 0, 1, 2")
        void simplePositions() {
            LexerResult result = lexer.tokenize("abc");
            List<Token> tokens = result.getTokens();

            assertEquals(0, tokens.get(0).getPosition());
            assertEquals(1, tokens.get(1).getPosition());
            assertEquals(2, tokens.get(2).getPosition());
        }

        @Test
        @DisplayName("'a\\*b' — позиции учитывают экранирование: 0, 1, 3")
        void positionsWithEscape() {
            LexerResult result = lexer.tokenize("a\\*b");
            List<Token> tokens = result.getTokens();

            assertEquals(0, tokens.get(0).getPosition()); // a на позиции 0
            assertEquals(1, tokens.get(1).getPosition()); // \* начинается на 1
            assertEquals(3, tokens.get(2).getPosition()); // b на позиции 3
        }

        @Test
        @DisplayName("'{123}' — позиция числа указывает на первую цифру")
        void positionsWithNumber() {
            LexerResult result = lexer.tokenize("{123}");
            List<Token> tokens = result.getTokens();

            assertEquals(0, tokens.get(0).getPosition()); // { на 0
            assertEquals(1, tokens.get(1).getPosition()); // 123 начинается на 1
            assertEquals(4, tokens.get(2).getPosition()); // } на 4
        }

        @Test
        @DisplayName("'\\1\\2' — позиции backref: 0, 2")
        void positionsWithBackref() {
            LexerResult result = lexer.tokenize("\\1\\2");
            List<Token> tokens = result.getTokens();

            assertEquals(0, tokens.get(0).getPosition()); // \1 на 0
            assertEquals(2, tokens.get(1).getPosition()); // \2 на 2
        }
    }

    // ==========================================
    // 11. LEXER RESULT
    // ==========================================

    @Nested
    @DisplayName("11. LexerResult")
    class LexerResultTests {

        @Test
        @DisplayName("originalInput сохраняется корректно")
        void originalInputPreserved() {
            String input = "(a|b)*";
            LexerResult result = lexer.tokenize(input);
            assertEquals(input, result.getOriginalInput());
        }

        @Test
        @DisplayName("Список токенов неизменяем (immutable)")
        void tokensAreImmutable() {
            LexerResult result = lexer.tokenize("abc");
            List<Token> tokens = result.getTokens();

            assertThrows(UnsupportedOperationException.class,
                    () -> tokens.add(new Token(TokenType.CHAR, "x", 0)));
        }

        @Test
        @DisplayName("size() возвращает корректное количество")
        void sizeIsCorrect() {
            assertEquals(0, lexer.tokenize("").size());
            assertEquals(1, lexer.tokenize("a").size());
            assertEquals(6, lexer.tokenize("(a|b)*").size());
        }

        @Test
        @DisplayName("isEmpty() корректно работает")
        void isEmptyIsCorrect() {
            assertTrue(lexer.tokenize("").isEmpty());
            assertFalse(lexer.tokenize("a").isEmpty());
        }

        @Test
        @DisplayName("toString() содержит исходную строку и токены")
        void toStringIsReadable() {
            LexerResult result = lexer.tokenize("a|b");
            String str = result.toString();

            assertNotNull(str);
            assertFalse(str.isEmpty());
        }
    }
}



@DisplayName("Lexer Stress Tests (40)")
class LexerStressTest {
    private Lexer lexer;

    @BeforeEach void setUp() { lexer = new Lexer(); }

    @Test void empty() { assertEquals(0, lexer.tokenize("").size()); }
    @Test void singleA() { assertEquals(1, lexer.tokenize("a").size()); }
    @Test void allMeta() { assertEquals(9, lexer.tokenize("|*()[]{}#").size()); }
    @Test void escapedAll() { assertEquals(9, lexer.tokenize("\\|\\*\\(\\)\\[\\]\\{\\}\\#").size()); }
    @Test void backslashEnd() { assertThrows(LexerException.class, () -> lexer.tokenize("\\")); }
    @Test void backref1() { assertEquals(TokenType.BACKREF, lexer.tokenize("\\1").getTokens().get(0).getType()); }
    @Test void backref9() { assertEquals(TokenType.BACKREF, lexer.tokenize("\\9").getTokens().get(0).getType()); }
    @Test void backref0isChar() { assertEquals(TokenType.CHAR, lexer.tokenize("\\0").getTokens().get(0).getType()); }
    @Test void number() { assertEquals("123", lexer.tokenize("123").getTokens().get(0).getValue()); }
    @Test void numberInBraces() { assertEquals(3, lexer.tokenize("{5}").size()); }
    @Test void longString() { assertEquals(1000, lexer.tokenize("a".repeat(1000)).size()); }
    @Test void mixedTokens() { assertEquals(6, lexer.tokenize("(a|b)*").size()); }
    @Test void unicode() { assertEquals(3, lexer.tokenize("абв").size()); }
    @Test void spaces() { assertEquals(3, lexer.tokenize("a b").size()); }
    @Test void escapedRegular() { assertEquals(TokenType.CHAR, lexer.tokenize("\\a").getTokens().get(0).getType()); }
    @Test void doubleBackslash() { assertEquals("\\", lexer.tokenize("\\\\").getTokens().get(0).getValue()); }
    @Test void pipe() { assertEquals(TokenType.PIPE, lexer.tokenize("|").getTokens().get(0).getType()); }
    @Test void star() { assertEquals(TokenType.STAR, lexer.tokenize("*").getTokens().get(0).getType()); }
    @Test void hash() { assertEquals(TokenType.EPSILON, lexer.tokenize("#").getTokens().get(0).getType()); }
    @Test void lparen() { assertEquals(TokenType.LPAREN, lexer.tokenize("(").getTokens().get(0).getType()); }
    @Test void rparen() { assertEquals(TokenType.RPAREN, lexer.tokenize(")").getTokens().get(0).getType()); }
    @Test void lbracket() { assertEquals(TokenType.LBRACKET, lexer.tokenize("[").getTokens().get(0).getType()); }
    @Test void rbracket() { assertEquals(TokenType.RBRACKET, lexer.tokenize("]").getTokens().get(0).getType()); }
    @Test void lbrace() { assertEquals(TokenType.LBRACE, lexer.tokenize("{").getTokens().get(0).getType()); }
    @Test void rbrace() { assertEquals(TokenType.RBRACE, lexer.tokenize("}").getTokens().get(0).getType()); }
    @Test void positionFirst() { assertEquals(0, lexer.tokenize("abc").getTokens().get(0).getPosition()); }
    @Test void positionSecond() { assertEquals(1, lexer.tokenize("abc").getTokens().get(1).getPosition()); }
    @Test void positionEscape() { assertEquals(1, lexer.tokenize("a\\*").getTokens().get(1).getPosition()); }
    @Test void positionNumber() { assertEquals(1, lexer.tokenize("{12}").getTokens().get(1).getPosition()); }
    @Test void immutableResult() {
        assertThrows(UnsupportedOperationException.class,
            () -> lexer.tokenize("a").getTokens().add(new Token(TokenType.CHAR, "x", 0)));
    }
    @Test void nullInput() { assertThrows(Exception.class, () -> lexer.tokenize(null)); }
    @Test void onlyBackslashes() { assertEquals(2, lexer.tokenize("\\\\\\\\").size()); }
    @Test void multiDigit() { assertEquals(1, lexer.tokenize("999").size()); }
    @Test void charAfterNumber() { assertEquals(2, lexer.tokenize("1a").size()); }
    @Test void escapedStar() { assertEquals("*", lexer.tokenize("\\*").getTokens().get(0).getValue()); }
    @Test void escapedHash() { assertEquals("#", lexer.tokenize("\\#").getTokens().get(0).getValue()); }
    @Test void backrefInContext() { assertEquals(4, lexer.tokenize("(a)\\1").size()); }
    @Test void complexFull() { assertEquals(15, lexer.tokenize("(a|b)*c{2}[xy]#").size()); }
    @Test void uppercaseChars() { assertEquals(TokenType.CHAR, lexer.tokenize("Z").getTokens().get(0).getType()); }
    @Test void punctuation() {
        lexer.tokenize("!@$%^&-+=:;,.<>?/~`").getTokens()
            .forEach(t -> assertEquals(TokenType.CHAR, t.getType()));
    }
}