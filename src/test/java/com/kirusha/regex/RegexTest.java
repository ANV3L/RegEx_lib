package com.kirusha.regex;

import com.kirusha.regex.engine.MatchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Regex Stress Tests (200+)")
class RegexStressTest {

    // ==========================================
    // 1. Литералы и базовый матчинг (20 тестов)
    // ==========================================
    @Nested
    @DisplayName("1. Литералы")
    class Literals {
        @Test
        void singleChar() {
            assertTrue(Regex.matches("a", "a"));
        }

        @Test
        void singleCharReject() {
            assertFalse(Regex.matches("a", "b"));
        }

        @Test
        void emptyInput() {
            assertFalse(Regex.matches("a", ""));
        }

        @Test
        void longerInput() {
            assertFalse(Regex.matches("a", "aa"));
        }

        @Test
        void twoChars() {
            assertTrue(Regex.matches("ab", "ab"));
        }

        @Test
        void threeChars() {
            assertTrue(Regex.matches("abc", "abc"));
        }

        @Test
        void mismatchMiddle() {
            assertFalse(Regex.matches("abc", "axc"));
        }

        @Test
        void digits() {
            assertTrue(Regex.matches("123", "123"));
        }

        @Test
        void space() {
            assertTrue(Regex.matches(" ", " "));
        }

        @Test
        void multipleSpaces() {
            assertTrue(Regex.matches("a b", "a b"));
        }

        @Test
        void unicodeCyrillic() {
            assertTrue(Regex.matches("привет", "привет"));
        }

        @Test
        void mixedCase() {
            assertFalse(Regex.matches("A", "a"));
        }

        @Test
        void longLiteral() {
            String s = "abcdefghijklmnopqrstuvwxyz";
            assertTrue(Regex.matches(s, s));
        }

        @Test
        void longLiteralMismatch() {
            assertFalse(Regex.matches("abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyZ"));
        }

        @Test
        void specialPrintable() {
            assertTrue(Regex.matches("~`!@$%^&-_+=:;,.<>?/", "~`!@$%^&-_+=:;,.<>?/"));
        }

        @Test
        void tab() {
            assertTrue(Regex.matches("\t", "\t"));
        }

        @Test
        void singleDigit() {
            assertTrue(Regex.matches("5", "5"));
        }

        @Test
        void punctuation() {
            assertTrue(Regex.matches(".,;:", ".,;:"));
        }

        @Test
        void emptyRejectsNonEmpty() {
            assertFalse(Regex.matches("a", "ab"));
        }

        @Test
        void prefixMismatch() {
            assertFalse(Regex.matches("ab", "a"));
        }
    }

    // ==========================================
    // 2. Экранирование метасимволов (15 тестов)
    // ==========================================
    @Nested
    @DisplayName("2. Экранирование")
    class Escaping {
        @Test
        void escapedStar() {
            assertTrue(Regex.matches("\\*", "*"));
        }

        @Test
        void escapedPipe() {
            assertTrue(Regex.matches("\\|", "|"));
        }

        @Test
        void escapedHash() {
            assertTrue(Regex.matches("\\#", "#"));
        }

        @Test
        void escapedLparen() {
            assertTrue(Regex.matches("\\(", "("));
        }

        @Test
        void escapedRparen() {
            assertTrue(Regex.matches("\\)", ")"));
        }

        @Test
        void escapedLbracket() {
            assertTrue(Regex.matches("\\[", "["));
        }

        @Test
        void escapedRbracket() {
            assertTrue(Regex.matches("\\]", "]"));
        }

        @Test
        void escapedLbrace() {
            assertTrue(Regex.matches("\\{", "{"));
        }

        @Test
        void escapedRbrace() {
            assertTrue(Regex.matches("\\}", "}"));
        }

        @Test
        void escapedBackslash() {
            assertTrue(Regex.matches("\\\\", "\\"));
        }

        @Test
        void escapedStarNotKleene() {
            assertFalse(Regex.matches("\\*", ""));
        }

        @Test
        void escapedPipeNotAlt() {
            assertFalse(Regex.matches("a\\|b", "a"));
        }

        @Test
        void escapedHashNotEpsilon() {
            assertFalse(Regex.matches("\\#", ""));
        }

        @Test
        void mixedEscapes() {
            assertTrue(Regex.matches("\\*\\|\\#", "*|#"));
        }

        @Test
        void escapedInMiddle() {
            assertTrue(Regex.matches("a\\*b", "a*b"));
        }
    }

    // ==========================================
    // 3. Эпсилон # (10 тестов)
    // ==========================================
    @Nested
    @DisplayName("3. Эпсилон")
    class Epsilon {
        @Test
        void epsilonMatchesEmpty() {
            assertTrue(Regex.matches("#", ""));
        }

        @Test
        void epsilonRejectsNonEmpty() {
            assertFalse(Regex.matches("#", "a"));
        }

        @Test
        void epsilonConcat() {
            assertTrue(Regex.matches("a#", "a"));
        }

        @Test
        void epsilonConcatRight() {
            assertTrue(Regex.matches("#a", "a"));
        }

        @Test
        void doubleEpsilon() {
            assertTrue(Regex.matches("##", ""));
        }

        @Test
        void epsilonInAlt() {
            assertTrue(Regex.matches("#|a", ""));
        }

        @Test
        void epsilonInAlt2() {
            assertTrue(Regex.matches("#|a", "a"));
        }

        @Test
        void tripleEpsilon() {
            assertTrue(Regex.matches("###", ""));
        }

        @Test
        void epsilonBetween() {
            assertTrue(Regex.matches("a#b", "ab"));
        }

        @Test
        void epsilonStar() {
            assertTrue(Regex.matches("#*", ""));
        }
    }

    // ==========================================
    // 4. Альтернация | (15 тестов)
    // ==========================================
    @Nested
    @DisplayName("4. Альтернация")
    class Alternation {
        @Test
        void simple() {
            assertTrue(Regex.matches("a|b", "a"));
        }

        @Test
        void simple2() {
            assertTrue(Regex.matches("a|b", "b"));
        }

        @Test
        void rejectOther() {
            assertFalse(Regex.matches("a|b", "c"));
        }

        @Test
        void rejectBoth() {
            assertFalse(Regex.matches("a|b", "ab"));
        }

        @Test
        void triple() {
            assertTrue(Regex.matches("a|b|c", "c"));
        }

        @Test
        void tripleReject() {
            assertFalse(Regex.matches("a|b|c", "d"));
        }

        @Test
        void withConcat() {
            assertTrue(Regex.matches("ab|cd", "ab"));
        }

        @Test
        void withConcat2() {
            assertTrue(Regex.matches("ab|cd", "cd"));
        }

        @Test
        void withConcatReject() {
            assertFalse(Regex.matches("ab|cd", "ac"));
        }

        @Test
        void nestedInGroup() {
            assertTrue(Regex.matches("(a|b)c", "ac"));
        }

        @Test
        void nestedInGroup2() {
            assertTrue(Regex.matches("(a|b)c", "bc"));
        }

        @Test
        void longAlternation() {
            assertTrue(Regex.matches("a|b|c|d|e|f", "f"));
        }

        @Test
        void longAltReject() {
            assertFalse(Regex.matches("a|b|c|d|e|f", "g"));
        }

        @Test
        void altWithStar() {
            assertTrue(Regex.matches("a*|b", ""));
        }

        @Test
        void altWithStar2() {
            assertTrue(Regex.matches("a*|b", "aaa"));
        }
    }

    // ==========================================
    // 5. Конкатенация (10 тестов)
    // ==========================================
    @Nested
    @DisplayName("5. Конкатенация")
    class Concatenation {
        @Test
        void two() {
            assertTrue(Regex.matches("ab", "ab"));
        }

        @Test
        void three() {
            assertTrue(Regex.matches("abc", "abc"));
        }

        @Test
        void ten() {
            assertTrue(Regex.matches("abcdefghij", "abcdefghij"));
        }

        @Test
        void withEpsilon() {
            assertTrue(Regex.matches("a#b", "ab"));
        }

        @Test
        void partialReject() {
            assertFalse(Regex.matches("abc", "ab"));
        }

        @Test
        void extraReject() {
            assertFalse(Regex.matches("ab", "abc"));
        }

        @Test
        void sameChar() {
            assertTrue(Regex.matches("aaa", "aaa"));
        }

        @Test
        void mismatchFirst() {
            assertFalse(Regex.matches("abc", "xbc"));
        }

        @Test
        void mismatchLast() {
            assertFalse(Regex.matches("abc", "abx"));
        }

        @Test
        void withGroup() {
            assertTrue(Regex.matches("(a)b", "ab"));
        }
    }

    // ==========================================
    // 6. Замыкание Клини * (20 тестов)
    // ==========================================
    @Nested
    @DisplayName("6. Замыкание Клини")
    class KleeneStar {
        @Test
        void emptyMatch() {
            assertTrue(Regex.matches("a*", ""));
        }

        @Test
        void oneMatch() {
            assertTrue(Regex.matches("a*", "a"));
        }

        @Test
        void multiMatch() {
            assertTrue(Regex.matches("a*", "aaaa"));
        }

        @Test
        void reject() {
            assertFalse(Regex.matches("a*", "b"));
        }

        @Test
        void rejectMixed() {
            assertFalse(Regex.matches("a*", "ab"));
        }

        @Test
        void starThenLiteral() {
            assertTrue(Regex.matches("a*b", "b"));
        }

        @Test
        void starThenLiteral2() {
            assertTrue(Regex.matches("a*b", "ab"));
        }

        @Test
        void starThenLiteral3() {
            assertTrue(Regex.matches("a*b", "aaab"));
        }

        @Test
        void starRejectNoEnd() {
            assertFalse(Regex.matches("a*b", "aaa"));
        }

        @Test
        void groupStar() {
            assertTrue(Regex.matches("(ab)*", ""));
        }

        @Test
        void groupStar2() {
            assertTrue(Regex.matches("(ab)*", "ab"));
        }

        @Test
        void groupStar3() {
            assertTrue(Regex.matches("(ab)*", "abab"));
        }

        @Test
        void groupStarReject() {
            assertFalse(Regex.matches("(ab)*", "aba"));
        }

        @Test
        void altStar() {
            assertTrue(Regex.matches("(a|b)*", "abba"));
        }

        @Test
        void altStarEmpty() {
            assertTrue(Regex.matches("(a|b)*", ""));
        }

        @Test
        void altStarReject() {
            assertFalse(Regex.matches("(a|b)*", "abc"));
        }

        @Test
        void doubleStar() {
            assertTrue(Regex.matches("a*b*", "aaabbb"));
        }

        @Test
        void doubleStarEmpty() {
            assertTrue(Regex.matches("a*b*", ""));
        }

        @Test
        void charClassStar() {
            assertTrue(Regex.matches("[ab]*", "abba"));
        }

        @Test
        void starLong() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100; i++)
                sb.append("a");
            assertTrue(Regex.matches("a*", sb.toString()));
        }
    }

    // ==========================================
    // 7. Символ из набора [] (15 тестов)
    // ==========================================
    @Nested
    @DisplayName("7. Набор символов []")
    class CharClass {
        @Test
        void single() {
            assertTrue(Regex.matches("[a]", "a"));
        }

        @Test
        void singleReject() {
            assertFalse(Regex.matches("[a]", "b"));
        }

        @Test
        void multi() {
            assertTrue(Regex.matches("[abc]", "b"));
        }

        @Test
        void multiReject() {
            assertFalse(Regex.matches("[abc]", "d"));
        }

        @Test
        void rejectMultiChar() {
            assertFalse(Regex.matches("[abc]", "ab"));
        }

        @Test
        void emptySet() {
            assertTrue(Regex.matches("[]", ""));
        }

        @Test
        void emptySetReject() {
            assertFalse(Regex.matches("[]", "a"));
        }

        @Test
        void withConcat() {
            assertTrue(Regex.matches("[ab]c", "ac"));
        }

        @Test
        void withConcat2() {
            assertTrue(Regex.matches("[ab]c", "bc"));
        }

        @Test
        void withStar() {
            assertTrue(Regex.matches("[abc]*", "abcabc"));
        }

        @Test
        void withRepeat() {
            assertTrue(Regex.matches("[ab]{3}", "aba"));
        }

        @Test
        void digits() {
            assertTrue(Regex.matches("[123]", "2"));
        }

        @Test
        void mixed() {
            assertTrue(Regex.matches("[a1]", "1"));
        }

        @Test
        void escapedInside() {
            assertTrue(Regex.matches("[\\*]", "*"));
        }

        @Test
        void large() {
            assertTrue(Regex.matches("[abcdefghij]", "j"));
        }
    }

    // ==========================================
    // 8. Повтор {n} (15 тестов)
    // ==========================================
    @Nested
    @DisplayName("8. Повтор {n}")
    class Repeat {
        @Test
        void zero() {
            assertTrue(Regex.matches("a{0}", ""));
        }

        @Test
        void zeroReject() {
            assertFalse(Regex.matches("a{0}", "a"));
        }

        @Test
        void one() {
            assertTrue(Regex.matches("a{1}", "a"));
        }

        @Test
        void oneReject() {
            assertFalse(Regex.matches("a{1}", "aa"));
        }

        @Test
        void two() {
            assertTrue(Regex.matches("a{2}", "aa"));
        }

        @Test
        void three() {
            assertTrue(Regex.matches("a{3}", "aaa"));
        }

        @Test
        void threeReject() {
            assertFalse(Regex.matches("a{3}", "aa"));
        }

        @Test
        void threeRejectMore() {
            assertFalse(Regex.matches("a{3}", "aaaa"));
        }

        @Test
        void five() {
            assertTrue(Regex.matches("a{5}", "aaaaa"));
        }

        @Test
        void groupRepeat() {
            assertTrue(Regex.matches("(ab){2}", "abab"));
        }

        @Test
        void groupRepeatReject() {
            assertFalse(Regex.matches("(ab){2}", "ab"));
        }

        @Test
        void charClassRepeat() {
            assertTrue(Regex.matches("[ab]{3}", "aba"));
        }

        @Test
        void charClassRepeat2() {
            assertTrue(Regex.matches("[ab]{3}", "bbb"));
        }

        @Test
        void repeatThenLiteral() {
            assertTrue(Regex.matches("a{2}b", "aab"));
        }

        @Test
        void repeatConcat() {
            assertTrue(Regex.matches("a{2}b{3}", "aabbb"));
        }
    }

    // ==========================================
    // 9. Группы захвата (20 тестов)
    // ==========================================
    @Nested
    @DisplayName("9. Группы захвата")
    class Groups {
        @Test
        void simpleGroup() {
            Regex r = Regex.compile("(a)b");
            MatchResult m = r.match("ab");
            assertTrue(m.matches());
            assertEquals("a", m.group(1));
        }

        @Test
        void twoGroups() {
            Regex r = Regex.compile("(a)(b)");
            MatchResult m = r.match("ab");
            assertEquals("a", m.group(1));
            assertEquals("b", m.group(2));
        }

        @Test
        void groupZero() {
            Regex r = Regex.compile("(a)b");
            MatchResult m = r.match("ab");
            assertEquals("ab", m.group(0));
        }

        @Test
        void nestedGroup() {
            Regex r = Regex.compile("((a)b)");
            MatchResult m = r.match("ab");
            assertEquals("ab", m.group(1));
            assertEquals("a", m.group(2));
        }

        @Test
        void threeGroups() {
            Regex r = Regex.compile("((a)(b))");
            MatchResult m = r.match("ab");
            assertEquals("ab", m.group(1));
            assertEquals("a", m.group(2));
            assertEquals("b", m.group(3));
        }

        @Test
        void groupWithAlt() {
            Regex r = Regex.compile("(a|b)c");
            assertEquals("a", r.match("ac").group(1));
        }

        @Test
        void groupWithAlt2() {
            Regex r = Regex.compile("(a|b)c");
            assertEquals("b", r.match("bc").group(1));
        }

        @Test
        void groupCount() {
            Regex r = Regex.compile("(a)(b)(c)");
            MatchResult m = r.match("abc");
            assertEquals(4, m.groupCount()); // 0 + 3 groups
        }

        @Test
        void noMatchNoGroups() {
            Regex r = Regex.compile("(a)b");
            MatchResult m = r.match("ac");
            assertFalse(m.matches());
        }

        @Test
        void iteratorWorks() {
            Regex r = Regex.compile("(x)(y)");
            MatchResult m = r.match("xy");
            int count = 0;
            for (String g : m)
                count++;
            assertEquals(3, count);
        }

        @Test
        void groupWithStar() {
            Regex r = Regex.compile("(a)*b");
            assertTrue(r.matches("aab"));
        }

        @Test
        void groupWithRepeat() {
            Regex r = Regex.compile("(a|b){2}c");
            MatchResult m = r.match("abc");
            assertTrue(m.matches());
        }

        @Test
        void deepNesting() {
            Regex r = Regex.compile("(((a)))");
            MatchResult m = r.match("a");
            assertEquals("a", m.group(1));
            assertEquals("a", m.group(2));
            assertEquals("a", m.group(3));
        }

        @Test
        void groupThenLiteral() {
            Regex r = Regex.compile("(ab)cd");
            MatchResult m = r.match("abcd");
            assertEquals("ab", m.group(1));
        }

        @Test
        void multiCharGroup() {
            Regex r = Regex.compile("(abc)");
            assertEquals("abc", r.match("abc").group(1));
        }

        @Test
        void groupAltInConcat() {
            Regex r = Regex.compile("a(b|c)d");
            assertEquals("b", r.match("abd").group(1));
        }

        @Test
        void groupAltInConcat2() {
            Regex r = Regex.compile("a(b|c)d");
            assertEquals("c", r.match("acd").group(1));
        }

        @Test
        void fourGroups() {
            Regex r = Regex.compile("(a)(b)(c)(d)");
            MatchResult m = r.match("abcd");
            assertEquals("a", m.group(1));
            assertEquals("d", m.group(4));
        }

        @Test
        void groupWithCharClass() {
            Regex r = Regex.compile("([ab])c");
            MatchResult m = r.match("ac");
            assertEquals("a", m.group(1));
        }

        @Test
        void emptyGroupContent() {
            Regex r = Regex.compile("(a|#)b");
            MatchResult m = r.match("b");
            assertTrue(m.matches());
        }
    }

    // ==========================================
    // 10. Обратные ссылки (10 тестов)
    // ==========================================
    @Nested
    @DisplayName("10. Обратные ссылки")
    class Backreferences {
        @Test
        void simple() {
            assertTrue(Regex.matches("(a)\\1", "aa"));
        }

        @Test
        void simpleReject() {
            assertFalse(Regex.matches("(a)\\1", "ab"));
        }

        @Test
        void withMiddle() {
            assertTrue(Regex.matches("(a)b\\1", "aba"));
        }

        @Test
        void multiChar() {
            assertTrue(Regex.matches("(ab)\\1", "abab"));
        }

        @Test
        void multiCharReject() {
            assertFalse(Regex.matches("(ab)\\1", "abba"));
        }

        @Test
        void twoGroups() {
            assertTrue(Regex.matches("(a)(b)\\1\\2", "abab"));
        }

        @Test
        void twoGroupsReject() {
            assertFalse(Regex.matches("(a)(b)\\1\\2", "abba"));
        }

        @Test
        void altBackref() {
            assertTrue(Regex.matches("(a|b)c\\1", "aca"));
        }

        @Test
        void altBackref2() {
            assertTrue(Regex.matches("(a|b)c\\1", "bcb"));
        }

        @Test
        void altBackrefReject() {
            assertFalse(Regex.matches("(a|b)c\\1", "acb"));
        }
    }

    // ==========================================
    // 11. Операторные скобки (приоритет) (10 тестов)
    // ==========================================
    @Nested
    @DisplayName("11. Приоритет скобок")
    class Precedence {
        @Test
        void starBeforeConcat() {
            assertTrue(Regex.matches("ab*", "a"));
        }

        @Test
        void starBeforeConcat2() {
            assertTrue(Regex.matches("ab*", "abbb"));
        }

        @Test
        void concatBeforeAlt() {
            assertTrue(Regex.matches("ab|cd", "ab"));
        }

        @Test
        void concatBeforeAlt2() {
            assertTrue(Regex.matches("ab|cd", "cd"));
        }

        @Test
        void concatBeforeAltReject() {
            assertFalse(Regex.matches("ab|cd", "ad"));
        }

        @Test
        void parensChangeOrder() {
            assertTrue(Regex.matches("(a|b)c", "ac"));
        }

        @Test
        void parensWithStar() {
            assertTrue(Regex.matches("(ab)*", "ababab"));
        }

        @Test
        void starOnGroup() {
            assertTrue(Regex.matches("(a|b)*c", "ababc"));
        }

        @Test
        void repeatOnGroup() {
            assertTrue(Regex.matches("(ab){3}", "ababab"));
        }

        @Test
        void complexPrecedence() {
            assertTrue(Regex.matches("a|bc*", "a"));
        }
    }

    // ==========================================
    // 12. Компиляция и compile() (10 тестов)
    // ==========================================
    @Nested
    @DisplayName("12. Компиляция")
    class Compilation {
        @Test
        void compileAndMatch() {
            assertTrue(Regex.compile("a*b").matches("aaab"));
        }

        @Test
        void compiledReuse() {
            Regex r = Regex.compile("a|b");
            assertTrue(r.matches("a"));
            assertTrue(r.matches("b"));
            assertFalse(r.matches("c"));
        }

        @Test
        void compileGetPattern() {
            assertEquals("abc", Regex.compile("abc").getPattern());
        }

        @Test
        void compileGetDFA() {
            assertNotNull(Regex.compile("a*").getDFA());
        }

        @Test
        void compileSyntaxError() {
            assertThrows(RuntimeException.class, () -> Regex.compile("a("));
        }

        @Test
        void compileSyntaxError2() {
            assertThrows(RuntimeException.class, () -> Regex.compile("a|"));
        }

        @Test
        void compileEpsilon() {
            assertTrue(Regex.compile("#").matches(""));
        }

        @Test
        void compileCharClass() {
            assertTrue(Regex.compile("[xyz]").matches("y"));
        }

        @Test
        void compileRepeat() {
            assertTrue(Regex.compile("a{4}").matches("aaaa"));
        }

        @Test
        void compileComplex() {
            assertTrue(Regex.compile("(a|b)*c{2}[xy]").matches("abccy"));
        }
    }

    // ==========================================
    // 13. Пересечение языков (10 тестов)
    // ==========================================
    @Nested
    @DisplayName("13. Пересечение")
    class Intersection {
        @Test
        void simpleIntersect() {
            String r = Regex.intersect("a|b", "a|c");
            assertTrue(Regex.compile(r).matches("a"));
            assertFalse(Regex.compile(r).matches("b"));
        }

        @Test
        void starIntersect() {
            String r = Regex.intersect("(a|b)*", "a*");
            Regex p = Regex.compile(r);
            assertTrue(p.matches("aaa"));
            assertFalse(p.matches("b"));
        }

        @Test
        void emptyIntersect() {
            String r = Regex.intersect("a", "b");
            assertFalse(Regex.compile(r).matches("a"));
        }

        @Test
        void sameIntersect() {
            String r = Regex.intersect("a*", "a*");
            assertTrue(Regex.compile(r).matches("aaa"));
        }

        @Test
        void intersectEmpty() {
            String r = Regex.intersect("(a|b)*", "a*");
            assertTrue(Regex.compile(r).matches(""));
        }

        @Test
        void intersectSingleChars() {
            String r = Regex.intersect("[abc]", "[bcd]");
            Regex p = Regex.compile(r);
            assertTrue(p.matches("b"));
            assertTrue(p.matches("c"));
            assertFalse(p.matches("a"));
            assertFalse(p.matches("d"));
        }

        @Test
        void intersectWithEpsilon() {
            String r = Regex.intersect("a*", "#|a");
            Regex p = Regex.compile(r);
            assertTrue(p.matches(""));
            assertTrue(p.matches("a"));
            assertFalse(p.matches("aa"));
        }

        @Test
        void intersectLong() {
            String r = Regex.intersect("(a|b|c)*", "(b|c|d)*");
            Regex p = Regex.compile(r);
            assertTrue(p.matches("bcbc"));
            assertFalse(p.matches("a"));
            assertFalse(p.matches("d"));
        }

        @Test
        void intersectDisjoint() {
            String r = Regex.intersect("a{3}", "a{2}");
            assertFalse(Regex.compile(r).matches("aa"));
            assertFalse(Regex.compile(r).matches("aaa"));
        }
    }

    // ==========================================
    // 14. Разность языков (10 тестов)
    // ==========================================
    @Nested
    @DisplayName("14. Разность")
    class Difference {
        @Test
        void simpleDiff() {
            String r = Regex.difference("a|b", "a");
            assertTrue(Regex.compile(r).matches("b"));
            assertFalse(Regex.compile(r).matches("a"));
        }

        @Test
        void starDiff() {
            String r = Regex.difference("(a|b)*", "a*");
            Regex p = Regex.compile(r);
            assertTrue(p.matches("b"));
            assertTrue(p.matches("ab"));
            assertFalse(p.matches("aaa"));
            assertFalse(p.matches(""));
        }

        @Test
        void sameDiff() {
            String r = Regex.difference("a*", "a*");
            assertFalse(Regex.compile(r).matches("a"));
            assertFalse(Regex.compile(r).matches(""));
        }

        @Test
        void diffFromSuperSet() {
            String r = Regex.difference("[abc]", "[ab]");
            Regex p = Regex.compile(r);
            assertTrue(p.matches("c"));
            assertFalse(p.matches("a"));
            assertFalse(p.matches("b"));
        }

        @Test
        void diffEmpty() {
            String r = Regex.difference("a", "b");
            assertTrue(Regex.compile(r).matches("a"));
        }

        @Test
        void diffNoOverlap() {
            String r = Regex.difference("a", "a");
            assertFalse(Regex.compile(r).matches("a"));
        }

        @Test
        void diffComplex() {
            String r = Regex.difference("(a|b|c)*", "(a|b)*");
            Regex p = Regex.compile(r);
            assertTrue(p.matches("c"));
            assertTrue(p.matches("ac"));
            assertFalse(p.matches("ab"));
            assertFalse(p.matches(""));
        }

        @Test
        void diffRepeat() {
            String r = Regex.difference("a{3}|a{2}", "a{2}");
            assertTrue(Regex.compile(r).matches("aaa"));
            assertFalse(Regex.compile(r).matches("aa"));
        }

        @Test
        void diffWithEpsilon() {
            String r = Regex.difference("a*", "#");
            Regex p = Regex.compile(r);
            assertFalse(p.matches(""));
            assertTrue(p.matches("a"));
        }

        @Test
        void diffLargeAlphabet() {
            String r = Regex.difference("[abcde]", "[abc]");
            Regex p = Regex.compile(r);
            assertTrue(p.matches("d"));
            assertTrue(p.matches("e"));
            assertFalse(p.matches("a"));
        }
    }

    // ==========================================
    // 15. Восстановление РВ из ДКА (10 тестов)
    // ==========================================
    @Nested
    @DisplayName("15. Восстановление")
    class Recovery {
        private void assertRecovery(String regex, String[] accept, String[] reject) {
            Regex r = Regex.compile(regex);
            String recovered = r.recover();
            assertNotNull(recovered);
            Regex rr = Regex.compile(recovered);
            for (String s : accept)
                assertTrue(rr.matches(s), "Should accept: " + s);
            for (String s : reject)
                assertFalse(rr.matches(s), "Should reject: " + s);
        }

        @Test
        void recoverLiteral() {
            assertRecovery("a", new String[] { "a" }, new String[] { "b", "" });
        }

        @Test
        void recoverConcat() {
            assertRecovery("ab", new String[] { "ab" }, new String[] { "a", "b" });
        }

        @Test
        void recoverAlt() {
            assertRecovery("a|b", new String[] { "a", "b" }, new String[] { "ab", "" });
        }

        @Test
        void recoverStar() {
            assertRecovery("a*", new String[] { "", "a", "aaa" }, new String[] { "b" });
        }

        @Test
        void recoverRepeat() {
            assertRecovery("a{3}", new String[] { "aaa" }, new String[] { "aa", "aaaa" });
        }

        @Test
        void recoverCharClass() {
            assertRecovery("[abc]", new String[] { "a", "b", "c" }, new String[] { "d" });
        }

        @Test
        void recoverComplex() {
            assertRecovery("(a|b)*c", new String[] { "c", "ac", "bc" }, new String[] { "", "a" });
        }

        @Test
        void recoverStarConcat() {
            assertRecovery("a*b", new String[] { "b", "ab", "aab" }, new String[] { "a" });
        }

        @Test
        void recoverMultiAlt() {
            assertRecovery("a|b|c", new String[] { "a", "b", "c" }, new String[] { "d" });
        }

        @Test
        void recoverGroupStar() {
            assertRecovery("(ab)*", new String[] { "", "ab", "abab" }, new String[] { "a" });
        }
    }

    // ==========================================
    // 16. Комбинированные сложные выражения (30 тестов)
    // ==========================================
    @Nested
    @DisplayName("16. Комбинированные")
    class Combined {
        @Test
        void starAltConcat() {
            assertTrue(Regex.matches("(a|b)*cd", "ababcd"));
        }

        @Test
        void starAltConcatReject() {
            assertFalse(Regex.matches("(a|b)*cd", "ababc"));
        }

        @Test
        void repeatCharClass() {
            assertTrue(Regex.matches("[abc]{2}[xy]", "abx"));
        }

        @Test
        void repeatCharClassReject() {
            assertFalse(Regex.matches("[abc]{2}[xy]", "abz"));
        }

        @Test
        void nestedGroupStar() {
            assertTrue(Regex.matches("((a|b)c)*", "acbc"));
        }

        @Test
        void nestedGroupStarEmpty() {
            assertTrue(Regex.matches("((a|b)c)*", ""));
        }

        @Test
        void starRepeatMix() {
            assertTrue(Regex.matches("a*b{2}c*", "aabbcc"));
        }

        @Test
        void complexEpsilon() {
            assertTrue(Regex.matches("(a|#)b(c|#)", "b"));
        }

        @Test
        void complexEpsilon2() {
            assertTrue(Regex.matches("(a|#)b(c|#)", "abc"));
        }

        @Test
        void complexEpsilon3() {
            assertTrue(Regex.matches("(a|#)b(c|#)", "ab"));
        }

        @Test
        void complexEpsilon4() {
            assertTrue(Regex.matches("(a|#)b(c|#)", "bc"));
        }

        @Test
        void deepNestingStar() {
            assertTrue(Regex.matches("(((a|b)*)c)*", "ababcabc"));
        }

        @Test
        void deepNestingStarEmpty() {
            assertTrue(Regex.matches("(((a|b)*)c)*", ""));
        }

        @Test
        void charClassThenStar() {
            assertTrue(Regex.matches("[abc]*d", "abcabcd"));
        }

        @Test
        void starThenCharClass() {
            assertTrue(Regex.matches("a*[bc]", "aaab"));
        }

        @Test
        void repeatAlt() {
            assertTrue(Regex.matches("(a|bc){3}", "abcbc"));
        }

        @Test
        void longConcat() {
            assertTrue(Regex.matches("a{10}", "aaaaaaaaaa"));
        }

        @Test
        void altCharClass() {
            assertTrue(Regex.matches("[ab]|c", "a"));
        }

        @Test
        void altCharClass2() {
            assertTrue(Regex.matches("[ab]|c", "c"));
        }

        @Test
        void starOfRepeat() {
            assertTrue(Regex.matches("(a{2})*", "aaaa"));
        }

        @Test
        void starOfRepeatOdd() {
            assertFalse(Regex.matches("(a{2})*", "aaa"));
        }

        @Test
        void escapedInPattern() {
            assertTrue(Regex.matches("a\\*b", "a*b"));
        }

        @Test
        void repeatZeroConcat() {
            assertTrue(Regex.matches("a{0}b", "b"));
        }

        @Test
        void emptyCharClassConcat() {
            assertTrue(Regex.matches("a[]b", "ab"));
        }

        @Test
        void tripleConcat() {
            assertTrue(Regex.matches("abc", "abc"));
        }

        @Test
        void starStarStar() {
            assertTrue(Regex.matches("a*b*c*", "aaabbbccc"));
        }

        @Test
        void starStarStarEmpty() {
            assertTrue(Regex.matches("a*b*c*", ""));
        }

        @Test
        void starStarStarPartial() {
            assertTrue(Regex.matches("a*b*c*", "bbb"));
        }

        @Test
        void complexFull() {
            assertTrue(Regex.matches("(a|b)*c{2}[xy]#", "abccx"));
        }

        @Test
        void veryLong() {
            StringBuilder pattern = new StringBuilder();
            StringBuilder input = new StringBuilder();
            for (int i = 0; i < 50; i++) {
                pattern.append("a");
                input.append("a");
            }
            assertTrue(Regex.matches(pattern.toString(), input.toString()));
        }
    }
}

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
