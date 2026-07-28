package com.kirusha.regex.parser;

import com.kirusha.regex.lexer.Lexer;
import com.kirusha.regex.lexer.LexerResult;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.AlternationNode;
import com.kirusha.regex.parser.ast.BackReferenceNode;
import com.kirusha.regex.parser.ast.CharClassNode;
import com.kirusha.regex.parser.ast.ConcatenationNode;
import com.kirusha.regex.parser.ast.EpsilonNode;
import com.kirusha.regex.parser.ast.GroupNode;
import com.kirusha.regex.parser.ast.KleeneStarNode;
import com.kirusha.regex.parser.ast.LiteralNode;
import com.kirusha.regex.parser.ast.RepeatNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Полное тестирование парсера.
 *
 * Проверяем:
 * 1. Базовые атомы
 * 2. Конкатенацию
 * 3. Альтернацию
 * 4. Приоритеты операторов
 * 5. Скобки и группы захвата
 * 6. Kleene star
 * 7. Repeat {n}
 * 8. CharClass []
 * 9. BackReference
 * 10. Комбинированные выражения
 * 11. Ошибки
 * 12. ParserResult и groupCount
 */
@DisplayName("Parser")
class ParserTest {

    private Lexer lexer;
    private Parser parser;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
        parser = new Parser();
    }

    /**
     * Утилита: прогнать строку через Lexer, потом через Parser.
     */
    private ParserResult parse(String regex) {
        LexerResult lexerResult = lexer.tokenize(regex);
        return parser.parse(lexerResult);
    }

    /**
     * Утилита: получить корень AST напрямую.
     */
    private ASTNode root(String regex) {
        return parse(regex).getRoot();
    }

    // ==========================================
    // 1. БАЗОВЫЕ АТОМЫ
    // ==========================================

    @Nested
    @DisplayName("1. Базовые атомы")
    class BasicAtoms {

        @Test
        @DisplayName("'a' -> LiteralNode")
        void singleLiteral() {
            ASTNode root = root("a");

            assertInstanceOf(LiteralNode.class, root);
            LiteralNode node = (LiteralNode) root;
            assertEquals("a", node.getValue());
        }

        @Test
        @DisplayName("'~' -> EpsilonNode")
        void epsilon() {
            ASTNode root = root("~");
            assertInstanceOf(EpsilonNode.class, root);
        }

        @Test
        @DisplayName("'\\1' -> BackReferenceNode")
        void backReference() {
            ASTNode root = root("\\1");

            assertInstanceOf(BackReferenceNode.class, root);
            BackReferenceNode node = (BackReferenceNode) root;
            assertEquals(1, node.getGroupNumber());
        }

        @Test
        @DisplayName("'[abc]' -> CharClassNode")
        void charClass() {
            ASTNode root = root("[abc]");

            assertInstanceOf(CharClassNode.class, root);
            CharClassNode node = (CharClassNode) root;
            assertEquals(List.of("a", "b", "c"), node.getSymbols());
        }

        @Test
        @DisplayName("'[]' -> пустой CharClassNode или EpsilonNode")
        void emptyCharClass() {
            ASTNode root = root("[]");

            assertTrue(
                    root instanceof CharClassNode || root instanceof EpsilonNode,
                    "Пустой набор должен разбираться либо в CharClassNode, либо в EpsilonNode"
            );

            if (root instanceof CharClassNode) {
                assertTrue(((CharClassNode) root).isEmpty());
            }
        }
    }

    // ==========================================
    // 2. КОНКАТЕНАЦИЯ
    // ==========================================

    @Nested
    @DisplayName("2. Конкатенация")
    class ConcatenationTests {

        @Test
        @DisplayName("'ab' -> Concat(Literal(a), Literal(b))")
        void simpleConcat() {
            ASTNode root = root("ab");

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode concat = (ConcatenationNode) root;

            assertInstanceOf(LiteralNode.class, concat.getLeft());
            assertInstanceOf(LiteralNode.class, concat.getRight());

            assertEquals("a", ((LiteralNode) concat.getLeft()).getValue());
            assertEquals("b", ((LiteralNode) concat.getRight()).getValue());
        }

        @Test
        @DisplayName("'abc' -> left-associative concat")
        void tripleConcat() {
            ASTNode root = root("abc");

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode outer = (ConcatenationNode) root;

            assertInstanceOf(ConcatenationNode.class, outer.getLeft());
            assertInstanceOf(LiteralNode.class, outer.getRight());

            ConcatenationNode inner = (ConcatenationNode) outer.getLeft();
            assertEquals("a", ((LiteralNode) inner.getLeft()).getValue());
            assertEquals("b", ((LiteralNode) inner.getRight()).getValue());
            assertEquals("c", ((LiteralNode) outer.getRight()).getValue());
        }

        @Test
        @DisplayName("'a~' -> Concat(Literal(a), Epsilon)")
        void concatWithEpsilon() {
            ASTNode root = root("a~");

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode concat = (ConcatenationNode) root;

            assertInstanceOf(LiteralNode.class, concat.getLeft());
            assertInstanceOf(EpsilonNode.class, concat.getRight());
        }
    }

    // ==========================================
    // 3. АЛЬТЕРНАЦИЯ
    // ==========================================

    @Nested
    @DisplayName("3. Альтернация")
    class AlternationTests {

        @Test
        @DisplayName("'a|b' -> Alternation(Literal(a), Literal(b))")
        void simpleAlternation() {
            ASTNode root = root("a|b");

            assertInstanceOf(AlternationNode.class, root);
            AlternationNode alt = (AlternationNode) root;

            assertInstanceOf(LiteralNode.class, alt.getLeft());
            assertInstanceOf(LiteralNode.class, alt.getRight());

            assertEquals("a", ((LiteralNode) alt.getLeft()).getValue());
            assertEquals("b", ((LiteralNode) alt.getRight()).getValue());
        }

        @Test
        @DisplayName("'a|b|c' -> associative alternation")
        void chainAlternation() {
            ASTNode root = root("a|b|c");

            assertInstanceOf(AlternationNode.class, root);
            AlternationNode outer = (AlternationNode) root;

            assertInstanceOf(AlternationNode.class, outer.getLeft());
            assertInstanceOf(LiteralNode.class, outer.getRight());

            AlternationNode inner = (AlternationNode) outer.getLeft();
            assertEquals("a", ((LiteralNode) inner.getLeft()).getValue());
            assertEquals("b", ((LiteralNode) inner.getRight()).getValue());
            assertEquals("c", ((LiteralNode) outer.getRight()).getValue());
        }
    }

    // ==========================================
    // 4. ПРИОРИТЕТЫ ОПЕРАТОРОВ
    // ==========================================

    @Nested
    @DisplayName("4. Приоритеты операторов")
    class PrecedenceTests {

        @Test
        @DisplayName("'a|bc' -> Alt(a, Concat(b, c))")
        void alternationLowerThanConcat() {
            ASTNode root = root("a|bc");

            assertInstanceOf(AlternationNode.class, root);
            AlternationNode alt = (AlternationNode) root;

            assertInstanceOf(LiteralNode.class, alt.getLeft());
            assertInstanceOf(ConcatenationNode.class, alt.getRight());

            assertEquals("a", ((LiteralNode) alt.getLeft()).getValue());

            ConcatenationNode concat = (ConcatenationNode) alt.getRight();
            assertEquals("b", ((LiteralNode) concat.getLeft()).getValue());
            assertEquals("c", ((LiteralNode) concat.getRight()).getValue());
        }

        @Test
        @DisplayName("'ab|c' -> Alt(Concat(a, b), c)")
        void concatHigherThanAlternation() {
            ASTNode root = root("ab|c");

            assertInstanceOf(AlternationNode.class, root);
            AlternationNode alt = (AlternationNode) root;

            assertInstanceOf(ConcatenationNode.class, alt.getLeft());
            assertInstanceOf(LiteralNode.class, alt.getRight());
        }

        @Test
        @DisplayName("'ab*' -> Star(Concat(a, b))")
        void starHigherThanConcat() {
            ASTNode root = root("ab*");

            assertInstanceOf(KleeneStarNode.class, root);
            KleeneStarNode star = (KleeneStarNode) root;

            assertInstanceOf(ConcatenationNode.class, star.getChild());
            ConcatenationNode concat = (ConcatenationNode) star.getChild();
            assertEquals("a", ((LiteralNode) concat.getLeft()).getValue());
            assertEquals("b", ((LiteralNode) concat.getRight()).getValue());
        }

        @Test
        @DisplayName("'a|b*' -> Alt(a, Star(b))")
        void starHigherThanAlternation() {
            ASTNode root = root("a|b*");

            assertInstanceOf(AlternationNode.class, root);
            AlternationNode alt = (AlternationNode) root;

            assertInstanceOf(LiteralNode.class, alt.getLeft());
            assertInstanceOf(KleeneStarNode.class, alt.getRight());
        }

        @Test
        @DisplayName("'a|bc*' -> Alt(a, Star(Concat(b, c)))")
        void fullPriorityExample() {
            ASTNode root = root("a|bc*");

            assertInstanceOf(AlternationNode.class, root);
            AlternationNode alt = (AlternationNode) root;

            assertInstanceOf(LiteralNode.class, alt.getLeft());
            assertInstanceOf(KleeneStarNode.class, alt.getRight());

            KleeneStarNode star = (KleeneStarNode) alt.getRight();
            assertInstanceOf(ConcatenationNode.class, star.getChild());
            ConcatenationNode concat = (ConcatenationNode) star.getChild();
            assertEquals("b", ((LiteralNode) concat.getLeft()).getValue());
            assertEquals("c", ((LiteralNode) concat.getRight()).getValue());
        }
    }

    // ==========================================
    // 5. СКОБКИ И ГРУППЫ ЗАХВАТА
    // ==========================================

    @Nested
    @DisplayName("5. Скобки и группы захвата")
    class GroupsTests {

        @Test
        @DisplayName("'(a)' -> GroupNode(1, Literal(a))")
        void singleGroup() {
            ParserResult result = parse("(a)");
            ASTNode root = result.getRoot();

            assertInstanceOf(GroupNode.class, root);
            GroupNode group = (GroupNode) root;

            assertEquals(1, group.getGroupNumber());
            assertInstanceOf(LiteralNode.class, group.getChild());
            assertEquals("a", ((LiteralNode) group.getChild()).getValue());
            assertEquals(1, result.getGroupCount());
        }

        @Test
        @DisplayName("'((a))' -> nested groups with numbering 1, 2")
        void nestedGroups() {
            ParserResult result = parse("((a))");
            ASTNode root = result.getRoot();

            assertInstanceOf(GroupNode.class, root);
            GroupNode outer = (GroupNode) root;
            assertEquals(1, outer.getGroupNumber());

            assertInstanceOf(GroupNode.class, outer.getChild());
            GroupNode inner = (GroupNode) outer.getChild();
            assertEquals(2, inner.getGroupNumber());

            assertInstanceOf(LiteralNode.class, inner.getChild());
            assertEquals("a", ((LiteralNode) inner.getChild()).getValue());

            assertEquals(2, result.getGroupCount());
        }

        @Test
        @DisplayName("'(ab)' -> Group(Concat(a, b))")
        void groupWithConcat() {
            ASTNode root = root("(ab)");

            assertInstanceOf(GroupNode.class, root);
            GroupNode group = (GroupNode) root;

            assertInstanceOf(ConcatenationNode.class, group.getChild());
        }

        @Test
        @DisplayName("'(a|b)' -> Group(Alt(a, b))")
        void groupWithAlternation() {
            ASTNode root = root("(a|b)");

            assertInstanceOf(GroupNode.class, root);
            GroupNode group = (GroupNode) root;

            assertInstanceOf(AlternationNode.class, group.getChild());
        }

        @Test
        @DisplayName("'(a)(b)' -> Concat(Group1(a), Group2(b))")
        void adjacentGroups() {
            ParserResult result = parse("(a)(b)");
            ASTNode root = result.getRoot();

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode concat = (ConcatenationNode) root;

            assertInstanceOf(GroupNode.class, concat.getLeft());
            assertInstanceOf(GroupNode.class, concat.getRight());

            GroupNode g1 = (GroupNode) concat.getLeft();
            GroupNode g2 = (GroupNode) concat.getRight();

            assertEquals(1, g1.getGroupNumber());
            assertEquals(2, g2.getGroupNumber());

            assertEquals(2, result.getGroupCount());
        }

        @Test
        @DisplayName("'((a)(b))' -> correct numbering 1, 2, 3")
        void deepGroupNumbering() {
            ParserResult result = parse("((a)(b))");
            ASTNode root = result.getRoot();

            assertInstanceOf(GroupNode.class, root);
            GroupNode g1 = (GroupNode) root;
            assertEquals(1, g1.getGroupNumber());

            assertInstanceOf(ConcatenationNode.class, g1.getChild());
            ConcatenationNode concat = (ConcatenationNode) g1.getChild();

            assertInstanceOf(GroupNode.class, concat.getLeft());
            assertInstanceOf(GroupNode.class, concat.getRight());

            GroupNode g2 = (GroupNode) concat.getLeft();
            GroupNode g3 = (GroupNode) concat.getRight();

            assertEquals(2, g2.getGroupNumber());
            assertEquals(3, g3.getGroupNumber());
            assertEquals(3, result.getGroupCount());
        }
    }

    // ==========================================
    // 6. KLEENE STAR
    // ==========================================

    @Nested
    @DisplayName("6. Kleene star")
    class StarTests {

        @Test
        @DisplayName("'a*' -> Star(Literal(a))")
        void starOverLiteral() {
            ASTNode root = root("a*");

            assertInstanceOf(KleeneStarNode.class, root);
            KleeneStarNode star = (KleeneStarNode) root;

            assertInstanceOf(LiteralNode.class, star.getChild());
            assertEquals("a", ((LiteralNode) star.getChild()).getValue());
        }

        @Test
        @DisplayName("'(ab)*' -> Star(Group(...))")
        void starOverGroup() {
            ASTNode root = root("(ab)*");

            assertInstanceOf(KleeneStarNode.class, root);
            KleeneStarNode star = (KleeneStarNode) root;

            assertInstanceOf(GroupNode.class, star.getChild());
        }

        @Test
        @DisplayName("'[ab]*' -> Star(CharClass)")
        void starOverCharClass() {
            ASTNode root = root("[ab]*");

            assertInstanceOf(KleeneStarNode.class, root);
            KleeneStarNode star = (KleeneStarNode) root;

            assertInstanceOf(CharClassNode.class, star.getChild());
        }
    }

    // ==========================================
    // 7. REPEAT {n}
    // ==========================================

    @Nested
    @DisplayName("7. Repeat {n}")
    class RepeatTests {

        @Test
        @DisplayName("'a{3}' -> Repeat(Literal(a), 3)")
        void repeatLiteral() {
            ASTNode root = root("a{3}");

            assertInstanceOf(RepeatNode.class, root);
            RepeatNode repeat = (RepeatNode) root;

            assertInstanceOf(LiteralNode.class, repeat.getChild());
            assertEquals("a", ((LiteralNode) repeat.getChild()).getValue());
            assertEquals(3, repeat.getCount());
        }

        @Test
        @DisplayName("'(ab){2}' -> Repeat(Group(...), 2)")
        void repeatGroup() {
            ASTNode root = root("(ab){2}");

            assertInstanceOf(RepeatNode.class, root);
            RepeatNode repeat = (RepeatNode) root;

            assertInstanceOf(GroupNode.class, repeat.getChild());
            assertEquals(2, repeat.getCount());
        }

        @Test
        @DisplayName("'[xy]{5}' -> Repeat(CharClass, 5)")
        void repeatCharClass() {
            ASTNode root = root("[xy]{5}");

            assertInstanceOf(RepeatNode.class, root);
            RepeatNode repeat = (RepeatNode) root;

            assertInstanceOf(CharClassNode.class, repeat.getChild());
            assertEquals(5, repeat.getCount());
        }

        @Test
        @DisplayName("'a{0}' -> Repeat count 0")
        void repeatZero() {
            ASTNode root = root("a{0}");

            assertInstanceOf(RepeatNode.class, root);
            RepeatNode repeat = (RepeatNode) root;
            assertEquals(0, repeat.getCount());
        }
    }

    // ==========================================
    // 8. CHAR CLASS
    // ==========================================

    @Nested
    @DisplayName("8. CharClass")
    class CharClassTests {

        @Test
        @DisplayName("'[a1~]' -> symbols a, 1, ~")
        void charClassMixedSymbols() {
            ASTNode root = root("[a1~]");

            assertInstanceOf(CharClassNode.class, root);
            CharClassNode node = (CharClassNode) root;

            assertEquals(List.of("a", "1", "~"), node.getSymbols());
        }

        @Test
        @DisplayName("'[\\*\\|]' -> symbols *, |")
        void charClassEscapedMeta() {
            ASTNode root = root("[\\*\\|]");

            assertInstanceOf(CharClassNode.class, root);
            CharClassNode node = (CharClassNode) root;

            assertEquals(List.of("*", "|"), node.getSymbols());
        }

        @Test
        @DisplayName("'[ab]c' -> Concat(CharClass([a,b]), Literal(c))")
        void charClassInConcat() {
            ASTNode root = root("[ab]c");

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode concat = (ConcatenationNode) root;

            assertInstanceOf(CharClassNode.class, concat.getLeft());
            assertInstanceOf(LiteralNode.class, concat.getRight());
        }
    }

    // ==========================================
    // 9. BACKREFERENCE
    // ==========================================

    @Nested
    @DisplayName("9. BackReference")
    class BackReferenceTests {

        @Test
        @DisplayName("'(a)\\1' -> Concat(Group(1,a), BackRef(1))")
        void groupThenBackref() {
            ParserResult result = parse("(a)\\1");
            ASTNode root = result.getRoot();

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode concat = (ConcatenationNode) root;

            assertInstanceOf(GroupNode.class, concat.getLeft());
            assertInstanceOf(BackReferenceNode.class, concat.getRight());

            GroupNode group = (GroupNode) concat.getLeft();
            BackReferenceNode backRef = (BackReferenceNode) concat.getRight();

            assertEquals(1, group.getGroupNumber());
            assertEquals(1, backRef.getGroupNumber());
        }

        @Test
        @DisplayName("'((a)(b))\\1' -> Concat(Group(...), BackRef(1))")
        void nestedGroupThenBackref() {
            ASTNode root = root("((a)(b))\\1");

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode concat = (ConcatenationNode) root;

            assertInstanceOf(GroupNode.class, concat.getLeft());
            assertInstanceOf(BackReferenceNode.class, concat.getRight());

            BackReferenceNode backRef = (BackReferenceNode) concat.getRight();
            assertEquals(1, backRef.getGroupNumber());
        }
    }

    // ==========================================
    // 10. КОМБИНИРОВАННЫЕ ВЫРАЖЕНИЯ
    // ==========================================

    @Nested
    @DisplayName("10. Комбинированные выражения")
    class CombinedExpressions {

        @Test
        @DisplayName("'(a|b)*c' -> Concat(Star(Group(Alt(a,b))), c)")
        void starThenConcat() {
            ASTNode root = root("(a|b)*c");

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode concat = (ConcatenationNode) root;

            assertInstanceOf(KleeneStarNode.class, concat.getLeft());
            assertInstanceOf(LiteralNode.class, concat.getRight());

            KleeneStarNode star = (KleeneStarNode) concat.getLeft();
            assertInstanceOf(GroupNode.class, star.getChild());

            GroupNode group = (GroupNode) star.getChild();
            assertInstanceOf(AlternationNode.class, group.getChild());

            assertEquals("c", ((LiteralNode) concat.getRight()).getValue());
        }

        @Test
        @DisplayName("'(a|b)*c{2}[xy]~' -> complex AST")
        void fullCombined() {
            ASTNode root = root("(a|b)*c{2}[xy]~");

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode c1 = (ConcatenationNode) root;

            assertInstanceOf(ConcatenationNode.class, c1.getLeft());
            assertInstanceOf(EpsilonNode.class, c1.getRight());

            ConcatenationNode c2 = (ConcatenationNode) c1.getLeft();
            assertInstanceOf(RepeatNode.class, c2.getLeft());
            assertInstanceOf(CharClassNode.class, c2.getRight());
        }

        @Test
        @DisplayName("'a(b|c)d' -> Concat(Concat(a, Group(Alt(b,c))), d)")
        void concatGroupConcat() {
            ASTNode root = root("a(b|c)d");

            assertInstanceOf(ConcatenationNode.class, root);
            ConcatenationNode outer = (ConcatenationNode) root;

            assertInstanceOf(ConcatenationNode.class, outer.getLeft());
            assertInstanceOf(LiteralNode.class, outer.getRight());

            ConcatenationNode inner = (ConcatenationNode) outer.getLeft();
            assertInstanceOf(LiteralNode.class, inner.getLeft());
            assertInstanceOf(GroupNode.class, inner.getRight());
        }

        @Test
        @DisplayName("'([ab]|c)*' -> Star(Group(Alt(CharClass, Literal(c))))")
        void starOverGroupedAlternation() {
            ASTNode root = root("([ab]|c)*");

            assertInstanceOf(KleeneStarNode.class, root);
            KleeneStarNode star = (KleeneStarNode) root;

            assertInstanceOf(GroupNode.class, star.getChild());
            GroupNode group = (GroupNode) star.getChild();

            assertInstanceOf(AlternationNode.class, group.getChild());
            AlternationNode alt = (AlternationNode) group.getChild();

            assertInstanceOf(CharClassNode.class, alt.getLeft());
            assertInstanceOf(LiteralNode.class, alt.getRight());
        }
    }

    // ==========================================
    // 11. ОШИБКИ
    // ==========================================

    @Nested
    @DisplayName("11. Ошибки")
    class ErrorTests {

        @Test
        @DisplayName("'(' -> ParserException (незакрытая скобка)")
        void unclosedGroup() {
            assertThrows(ParserException.class, () -> parse("("));
        }

        @Test
        @DisplayName("'a)' -> ParserException (лишняя закрывающая скобка)")
        void unexpectedClosingParen() {
            assertThrows(ParserException.class, () -> parse("a)"));
        }

        @Test
        @DisplayName("'a|' -> ParserException (пустая правая часть альтернации)")
        void emptyRightAlternation() {
            assertThrows(ParserException.class, () -> parse("a|"));
        }

        @Test
        @DisplayName("'|a' -> ParserException (пустая левая часть альтернации)")
        void emptyLeftAlternation() {
            assertThrows(ParserException.class, () -> parse("|a"));
        }

        @Test
        @DisplayName("'a{}' -> ParserException (повтор без числа)")
        void repeatWithoutNumber() {
            assertThrows(ParserException.class, () -> parse("a{}"));
        }

        @Test
        @DisplayName("'a{b}' -> ParserException (в повторе не число)")
        void repeatWithNonNumber() {
            assertThrows(ParserException.class, () -> parse("a{b}"));
        }

        @Test
        @DisplayName("'a{3' -> ParserException (нет закрывающей фигурной скобки)")
        void unclosedRepeat() {
            assertThrows(ParserException.class, () -> parse("a{3"));
        }

        @Test
        @DisplayName("'[' -> ParserException (незакрытый набор)")
        void unclosedCharClass() {
            assertThrows(ParserException.class, () -> parse("["));
        }

        @Test
        @DisplayName("'[ab' -> ParserException (незакрытый набор)")
        void unclosedCharClassWithContent() {
            assertThrows(ParserException.class, () -> parse("[ab"));
        }

        @Test
        @DisplayName("Пустой ввод -> ParserException")
        void emptyInput() {
            assertThrows(ParserException.class, () -> parse(""));
        }

        @Test
        @DisplayName("null LexerResult -> исключение")
        void nullLexerResult() {
            assertThrows(Exception.class, () -> parser.parse(null));
        }
    }

    // ==========================================
    // 12. PARSER RESULT
    // ==========================================

    @Nested
    @DisplayName("12. ParserResult")
    class ParserResultTests {

        @Test
        @DisplayName("groupCount для regex без групп = 0")
        void noGroups() {
            ParserResult result = parse("ab|c");
            assertEquals(0, result.getGroupCount());
        }

        @Test
        @DisplayName("groupCount для '(a)(b(c))' = 3")
        void groupCount() {
            ParserResult result = parse("(a)(b(c))");
            assertEquals(3, result.getGroupCount());
        }

        @Test
        @DisplayName("originalInput сохраняется")
        void originalInputPreserved() {
            ParserResult result = parse("(a|b)*c");
            assertEquals("(a|b)*c", result.getOriginalInput());
        }

        @Test
        @DisplayName("root не должен быть null")
        void rootNotNull() {
            ParserResult result = parse("a");
            assertNotNull(result.getRoot());
        }

        @Test
        @DisplayName("toString() возвращает непустую строку")
        void toStringReadable() {
            ParserResult result = parse("a|b");
            String s = result.toString();

            assertNotNull(s);
            assertFalse(s.isEmpty());
        }
    }
}



@DisplayName("Parser Stress Tests (40)")
class ParserStressTest {
    private Lexer lexer;
    private Parser parser;

    @BeforeEach void setUp() { lexer = new Lexer(); parser = new Parser(); }

    private ASTNode root(String s) { return parser.parse(lexer.tokenize(s)).getRoot(); }
    private ParserResult parse(String s) { return parser.parse(lexer.tokenize(s)); }

    @Test void literal() { assertInstanceOf(LiteralNode.class, root("a")); }
    @Test void epsilon() { assertInstanceOf(EpsilonNode.class, root("~")); }
    @Test void backref() { assertInstanceOf(BackReferenceNode.class, root("\\1")); }
    @Test void charClass() { assertInstanceOf(CharClassNode.class, root("[abc]")); }
    @Test void emptyCharClass() { assertTrue(root("[]") instanceof CharClassNode || root("[]") instanceof EpsilonNode); }
    @Test void concat() { assertInstanceOf(ConcatenationNode.class, root("ab")); }
    @Test void alt() { assertInstanceOf(AlternationNode.class, root("a|b")); }
    @Test void star() { assertInstanceOf(KleeneStarNode.class, root("a*")); }
    @Test void repeat() { assertInstanceOf(RepeatNode.class, root("a{3}")); }
    @Test void group() { assertInstanceOf(GroupNode.class, root("(a)")); }
    @Test void groupCount0() { assertEquals(0, parse("abc").getGroupCount()); }
    @Test void groupCount1() { assertEquals(1, parse("(a)b").getGroupCount()); }
    @Test void groupCount3() { assertEquals(3, parse("(a)(b(c))").getGroupCount()); }
    @Test void precedenceAltConcat() { assertInstanceOf(AlternationNode.class, root("a|bc")); }
    @Test void precedenceStarConcat() { assertInstanceOf(KleeneStarNode.class, root("ab*")); }
    @Test void nestedGroup() { assertEquals(2, parse("((a))").getGroupCount()); }
    @Test void repeatZero() { assertEquals(0, ((RepeatNode) root("a{0}")).getCount()); }
    @Test void repeatFive() { assertEquals(5, ((RepeatNode) root("a{5}")).getCount()); }
    @Test void errorUnclosed() { assertThrows(ParserException.class, () -> parse("(")); }
    @Test void errorExtraClose() { assertThrows(ParserException.class, () -> parse("a)")); }
    @Test void errorEmptyAlt() { assertThrows(ParserException.class, () -> parse("a|")); }
    @Test void errorLeftAlt() { assertThrows(ParserException.class, () -> parse("|a")); }
    @Test void errorRepeatNoNum() { assertThrows(ParserException.class, () -> parse("a{}")); }
    @Test void errorRepeatNonNum() { assertThrows(ParserException.class, () -> parse("a{b}")); }
    @Test void errorUnclosedRepeat() { assertThrows(ParserException.class, () -> parse("a{3")); }
    @Test void errorUnclosedBracket() { assertThrows(ParserException.class, () -> parse("[")); }
    @Test void errorEmpty() { assertThrows(ParserException.class, () -> parse("")); }
    @Test void errorNull() { assertThrows(Exception.class, () -> parser.parse(null)); }
    @Test void originalInput() { assertEquals("a|b", parse("a|b").getOriginalInput()); }
    @Test void rootNotNull() { assertNotNull(parse("a").getRoot()); }
    @Test void tripleAlt() { assertInstanceOf(AlternationNode.class, root("a|b|c")); }
    @Test void tripleConcat() { assertInstanceOf(ConcatenationNode.class, root("abc")); }
    @Test void groupWithAlt() { assertInstanceOf(GroupNode.class, root("(a|b)")); }
    @Test void starOverGroup() { assertInstanceOf(KleeneStarNode.class, root("(ab)*")); }
    @Test void repeatOverGroup() { assertInstanceOf(RepeatNode.class, root("(ab){2}")); }
    @Test void charClassConcat() { assertInstanceOf(ConcatenationNode.class, root("[ab]c")); }
    @Test void backrefParsed() { assertEquals(1, ((BackReferenceNode) root("\\1")).getGroupNumber()); }
    @Test void groupNumber() { assertEquals(1, ((GroupNode) root("(a)")).getGroupNumber()); }
    @Test void deepGroup() { assertEquals(3, parse("((a)(b))").getGroupCount()); }
    @Test void concatEpsilon() {
        ConcatenationNode c = (ConcatenationNode) root("a~");
        assertInstanceOf(EpsilonNode.class, c.getRight());
    }
}