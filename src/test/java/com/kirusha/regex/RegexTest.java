package com.kirusha.regex;

import com.kirusha.regex.engine.MatchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Top-level Regex Library API Tests")
class RegexTest {

    // ==========================================
    // 1. Компиляция
    // ==========================================
    @Nested
    @DisplayName("Компиляция и базовое API")
    class CompilationAndApi {
        @Test
        @DisplayName("Успешная компиляция")
        void successfulCompile() {
            Regex regex = Regex.compile("a(b|c)*");
            assertNotNull(regex);
            assertEquals("a(b|c)*", regex.getPattern());
            assertNotNull(regex.getDFA());
        }

        @Test
        @DisplayName("Синтаксическая ошибка")
        void syntaxError() {
            assertThrows(RuntimeException.class, () -> Regex.compile("a(b"));
        }

        @Test
        @DisplayName("Компиляция пустого #")
        void compileEpsilon() {
            Regex regex = Regex.compile("#");
            assertTrue(regex.matches(""));
            assertFalse(regex.matches("a"));
        }

        @Test
        @DisplayName("Компиляция char class")
        void compileCharClass() {
            Regex regex = Regex.compile("[abc]");
            assertTrue(regex.matches("a"));
            assertTrue(regex.matches("b"));
            assertTrue(regex.matches("c"));
            assertFalse(regex.matches("d"));
            assertFalse(regex.matches("ab"));
        }

        @Test
        @DisplayName("Компиляция repeat")
        void compileRepeat() {
            Regex regex = Regex.compile("a{3}");
            assertTrue(regex.matches("aaa"));
            assertFalse(regex.matches("aa"));
            assertFalse(regex.matches("aaaa"));
        }
    }

    // ==========================================
    // 2. Матчинг без групп (DFA)
    // ==========================================
    @Nested
    @DisplayName("Матчинг без групп (DFA)")
    class MatchWithoutGroups {
        @Test
        void literalMatch() {
            Regex r = Regex.compile("abc");
            assertTrue(r.matches("abc"));
            assertFalse(r.matches("ab"));
            assertFalse(r.matches("abcd"));
            assertFalse(r.matches(""));
        }

        @Test
        void starMatch() {
            Regex r = Regex.compile("a*b");
            assertTrue(r.matches("b"));
            assertTrue(r.matches("ab"));
            assertTrue(r.matches("aab"));
            assertTrue(r.matches("aaab"));
            assertFalse(r.matches("a"));
            assertFalse(r.matches(""));
            assertFalse(r.matches("ba"));
        }

        @Test
        void alternationMatch() {
            Regex r = Regex.compile("a|b|c");
            assertTrue(r.matches("a"));
            assertTrue(r.matches("b"));
            assertTrue(r.matches("c"));
            assertFalse(r.matches("d"));
            assertFalse(r.matches("ab"));
        }

        @Test
        void complexPattern() {
            Regex r = Regex.compile("(a|b)*c{2}[xy]");
            assertTrue(r.matches("ccx"));
            assertTrue(r.matches("abccy"));
            assertTrue(r.matches("aaccx"));
            assertFalse(r.matches("cc"));
            assertFalse(r.matches("ccz"));
        }

        @Test
        void staticMatchesMethod() {
            assertTrue(Regex.matches("a*b", "aab"));
            assertFalse(Regex.matches("a*b", "a"));
        }

        @Test
        void escapedMetaCharacters() {
            Regex r = Regex.compile("\\*\\|\\#");
            assertTrue(r.matches("*|#"));
            assertFalse(r.matches("a"));
        }

        @Test
        void repeatZero() {
            Regex r = Regex.compile("a{0}b");
            assertTrue(r.matches("b"));
            assertFalse(r.matches("ab"));
        }

        @Test
        void emptyCharClassAsEpsilon() {
            Regex r = Regex.compile("a[]b");
            assertTrue(r.matches("ab"));
        }
    }

    // ==========================================
    // 3. Матчинг с группами (NFA)
    // ==========================================
    @Nested
    @DisplayName("Матчинг с группами (NFA)")
    class MatchWithGroups {
        @Test
        @DisplayName("Простые группы (a)b")
        void simpleGroup() {
            Regex r = Regex.compile("(a)b");
            assertTrue(r.matches("ab"));
            assertFalse(r.matches("a"));
        }

        @Test
        @DisplayName("match() возвращает правильные группы для (a)b(c)")
        void matchResultGroups() {
            Regex r = Regex.compile("(a)b(c)");
            MatchResult result = r.match("abc");
            assertTrue(result.matches());
            assertEquals("abc", result.group(0));
            assertEquals("a", result.group(1));
            assertEquals("c", result.group(2));
        }

        @Test
        @DisplayName("Вложенные группы ((a)b)")
        void nestedGroups() {
            Regex r = Regex.compile("((a)b)");
            MatchResult result = r.match("ab");
            assertTrue(result.matches());
            assertEquals("ab", result.group(0));
            assertEquals("ab", result.group(1));
            assertEquals("a", result.group(2));
        }

        @Test
        @DisplayName("Три группы ((a)(b))")
        void threeGroups() {
            Regex r = Regex.compile("((a)(b))");
            MatchResult result = r.match("ab");
            assertTrue(result.matches());
            assertEquals("ab", result.group(1));
            assertEquals("a", result.group(2));
            assertEquals("b", result.group(3));
        }

        @Test
        @DisplayName("Группа с альтернацией (a|b)")
        void groupWithAlternation() {
            Regex r = Regex.compile("(a|b)c");
            MatchResult res1 = r.match("ac");
            assertTrue(res1.matches());
            assertEquals("a", res1.group(1));

            MatchResult res2 = r.match("bc");
            assertTrue(res2.matches());
            assertEquals("b", res2.group(1));
        }

        @Test
        @DisplayName("Repeat с группой — последняя итерация сохраняется")
        void repeatGroup() {
            Regex r = Regex.compile("(a|b){2}c");
            MatchResult result = r.match("bac");
            assertTrue(result.matches());
            assertEquals("a", result.group(1)); // последняя итерация
        }

        @Test
        @DisplayName("Итератор по группам")
        void matchResultIterable() {
            Regex r = Regex.compile("(x)(y)");
            MatchResult result = r.match("xy");
            int count = 0;
            for (String group : result) {
                count++;
            }
            assertEquals(3, count);
        }

        @Test
        @DisplayName("Несовпадение — noMatch")
        void noMatch() {
            Regex r = Regex.compile("(a)b");
            MatchResult result = r.match("ac");
            assertFalse(result.matches());
        }

        @Test
        @DisplayName("group(0) всегда вся строка")
        void groupZeroIsFullMatch() {
            Regex r = Regex.compile("(a)(b)(c)");
            MatchResult result = r.match("abc");
            assertEquals("abc", result.group(0));
        }
    }

    // ==========================================
    // 4. Обратные ссылки
    // ==========================================
    @Nested
    @DisplayName("Обратные ссылки (backreferences)")
    class Backreferences {
        @Test
        @DisplayName("(a|b)c\\1 — совпадение")
        void simpleBackref() {
            Regex r = Regex.compile("(a|b)c\\1");
            assertTrue(r.matches("aca"));
            assertTrue(r.matches("bcb"));
            assertFalse(r.matches("acb"));
            assertFalse(r.matches("bca"));
        }

        @Test
        @DisplayName("(ab)\\1 — многосимвольная группа")
        void multiCharBackref() {
            Regex r = Regex.compile("(ab)\\1");
            assertTrue(r.matches("abab"));
            assertFalse(r.matches("abba"));
            assertFalse(r.matches("ab"));
        }

        @Test
        @DisplayName("(a)(b)\\1\\2 — две группы")
        void twoGroupBackrefs() {
            Regex r = Regex.compile("(a)(b)\\1\\2");
            assertTrue(r.matches("abab"));
            assertFalse(r.matches("abba"));
        }
    }

    // ==========================================
    // 5. Операции над языками
    // ==========================================
    @Nested
    @DisplayName("Операции над языками")
    class SetsOperations {
        @Test
        @DisplayName("Пересечение (a|b)* ∩ a* = a*")
        void intersection() {
            String inter = Regex.intersect("(a|b)*", "a*");
            Regex r = Regex.compile(inter);
            assertTrue(r.matches(""));
            assertTrue(r.matches("a"));
            assertTrue(r.matches("aaa"));
            assertFalse(r.matches("b"));
            assertFalse(r.matches("ab"));
        }

        @Test
        @DisplayName("Разность (a|b)* \\ a* = строки с хотя бы одной b")
        void difference() {
            String diff = Regex.difference("(a|b)*", "a*");
            Regex r = Regex.compile(diff);
            assertFalse(r.matches(""));
            assertFalse(r.matches("a"));
            assertFalse(r.matches("aaa"));
            assertTrue(r.matches("b"));
            assertTrue(r.matches("ab"));
            assertTrue(r.matches("ba"));
            assertTrue(r.matches("abb"));
        }

        @Test
        @DisplayName("Пересечение непересекающихся языков = пустой")
        void emptyIntersection() {
            String inter = Regex.intersect("a", "b");
            Regex r = Regex.compile(inter);
            assertFalse(r.matches("a"));
            assertFalse(r.matches("b"));
            assertFalse(r.matches(""));
        }

        @Test
        @DisplayName("Разность одинаковых = пустой")
        void differenceOfSame() {
            String diff = Regex.difference("a*", "a*");
            Regex r = Regex.compile(diff);
            assertFalse(r.matches(""));
            assertFalse(r.matches("a"));
        }
    }

    // ==========================================
    // 6. Восстановление РВ из ДКА
    // ==========================================
    @Nested
    @DisplayName("Восстановление из ДКА")
    class RecoveryOperation {
        @Test
        @DisplayName("recover() для a|b")
        void recoverAlternation() {
            Regex r = Regex.compile("a|b");
            String recovered = r.recover();
            assertNotNull(recovered);
            Regex rr = Regex.compile(recovered);
            assertTrue(rr.matches("a"));
            assertTrue(rr.matches("b"));
            assertFalse(rr.matches("ab"));
            assertFalse(rr.matches(""));
        }

        @Test
        @DisplayName("recover() для a*")
        void recoverStar() {
            Regex r = Regex.compile("a*");
            String recovered = r.recover();
            Regex rr = Regex.compile(recovered);
            assertTrue(rr.matches(""));
            assertTrue(rr.matches("a"));
            assertTrue(rr.matches("aaa"));
            assertFalse(rr.matches("b"));
        }

        @Test
        @DisplayName("recover() для (a|b)*c")
        void recoverComplex() {
            Regex r = Regex.compile("(a|b)*c");
            String recovered = r.recover();
            Regex rr = Regex.compile(recovered);
            assertTrue(rr.matches("c"));
            assertTrue(rr.matches("ac"));
            assertTrue(rr.matches("bc"));
            assertTrue(rr.matches("ababc"));
            assertFalse(rr.matches(""));
            assertFalse(rr.matches("a"));
        }
    }
}
