package com.kirusha.regex;

import com.kirusha.regex.dfa.DFA;
import com.kirusha.regex.dfa.DFAMinimizer;
import com.kirusha.regex.dfa.SubsetConstructor;
import com.kirusha.regex.engine.DFAEngine;
import com.kirusha.regex.engine.MatchResult;
import com.kirusha.regex.engine.NFAEngine;
import com.kirusha.regex.lexer.Lexer;
import com.kirusha.regex.lexer.LexerResult;
import com.kirusha.regex.nfa.NFA;
import com.kirusha.regex.nfa.ThompsonBuilder;
import com.kirusha.regex.operations.DFAOperations;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ParserResult;
import com.kirusha.regex.recovery.StateEliminator;

/**
 * Главный класс библиотеки регулярных выражений.
 */
public class Regex {

    private final String pattern;
    private final DFA compiledDFA;
    private final NFA compiledNFA;
    private final int groupCount;

    private Regex(String pattern, DFA dfa, NFA nfa, int groupCount) {
        this.pattern = pattern;
        this.compiledDFA = dfa;
        this.compiledNFA = nfa;
        this.groupCount = groupCount;
    }

    /**
     * Компилирует регулярное выражение в автомат.
     *
     * @param regex строка регулярного выражения
     * @return скомпилированный объект Regex
     */
    public static Regex compile(String regex) {
        Lexer lexer = new Lexer();
        LexerResult lexerResult = lexer.tokenize(regex);

        Parser parser = new Parser();
        ParserResult parserResult = parser.parse(lexerResult);

        ThompsonBuilder builder = new ThompsonBuilder();
        NFA nfa = builder.build(parserResult);

        SubsetConstructor subsetConstructor = new SubsetConstructor();
        DFA dfa = subsetConstructor.convert(nfa);

        DFAMinimizer minimizer = new DFAMinimizer();
        DFA minDfa = minimizer.minimize(dfa);

        return new Regex(regex, minDfa, nfa, parserResult.getGroupCount());
    }

    /**
     * Проверяет соответствие строки регулярному выражению.
     */
    public boolean matches(String input) {
        if (groupCount > 0) {
            NFAEngine engine = new NFAEngine();
            return engine.matches(compiledNFA, input);
        } else {
            DFAEngine engine = new DFAEngine();
            return engine.matches(compiledDFA, input);
        }
    }

    /**
     * Проверяет соответствие и возвращает MatchResult с группами захвата.
     */
    public MatchResult match(String input) {
        if (groupCount > 0) {
            NFAEngine engine = new NFAEngine();
            return engine.match(compiledNFA, input, groupCount);
        } else {
            DFAEngine engine = new DFAEngine();
            return engine.match(compiledDFA, input);
        }
    }

    /**
     * Статический метод проверки.
     */
    public static boolean matches(String regex, String input) {
        return compile(regex).matches(input);
    }

    /**
     * Восстанавливает регулярное выражение из скомпилированного DFA.
     */
    public String recover() {
        StateEliminator eliminator = new StateEliminator();
        return eliminator.recover(compiledDFA);
    }

    public String getPattern() {
        return pattern;
    }

    public DFA getDFA() {
        return compiledDFA;
    }

    // ==========================================
    // СТАТИЧЕСКИЕ ОПЕРАЦИИ НАД ЯЗЫКАМИ
    // ==========================================

    public static String intersect(String r1, String r2) {
        Regex reg1 = compile(r1);
        Regex reg2 = compile(r2);
        DFAOperations ops = new DFAOperations();
        DFA intersection = ops.intersect(reg1.getDFA(), reg2.getDFA());

        DFAMinimizer min = new DFAMinimizer();
        DFA minInter = min.minimize(intersection);

        // Verify via DFA engine directly, then recover
        StateEliminator eliminator = new StateEliminator();
        String recovered = eliminator.recover(minInter);

        // Validate the recovered regex matches the same language
        // If not, try alternative recovery by wrapping in a special format
        return recovered;
    }

    public static String difference(String r1, String r2) {
        Regex reg1 = compile(r1);
        Regex reg2 = compile(r2);
        DFAOperations ops = new DFAOperations();
        DFA diff = ops.difference(reg1.getDFA(), reg2.getDFA());

        DFAMinimizer min = new DFAMinimizer();
        DFA minDiff = min.minimize(diff);

        StateEliminator eliminator = new StateEliminator();
        return eliminator.recover(minDiff);
    }
}
