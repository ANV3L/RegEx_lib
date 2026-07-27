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

    public static String process(String s) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < s.length()) {

            if (i + 4 < s.length()
                && s.charAt(i) == '['
                && isDigit(s.charAt(i + 1))
                && s.charAt(i + 2) == '-'
                && isDigit(s.charAt(i + 3))
                && s.charAt(i + 4) == ']') {
                
                int from = s.charAt(i + 1) - '0';
                int to = s.charAt(i + 3) - '0';
                
                result.append('[');
                if (from <= to) {
                    for (int d = from; d <= to; d++) {
                        result.append(d);
                    }
                } else {
                    result.append(s, i + 1, i + 4);
                }
                result.append(']');
                i += 5;
            }

            else if (i + 4 < s.length()
                && s.charAt(i) == '['
                && isLowercaseLetter(s.charAt(i + 1))
                && s.charAt(i + 2) == '-'
                && isLowercaseLetter(s.charAt(i + 3))
                && s.charAt(i + 4) == ']') {
                
                char from = s.charAt(i + 1);
                char to = s.charAt(i + 3);
                
                result.append('[');
                if (from <= to) {
                    for (char c = from; c <= to; c++) {
                        result.append(c);
                    }
                } else {
                    result.append(s, i + 1, i + 4);
                }
                result.append(']');
                i += 5;
            }
            
            else if (i + 4 < s.length()
                && s.charAt(i) == '['
                && isUppercaseLetter(s.charAt(i + 1))
                && s.charAt(i + 2) == '-'
                && isUppercaseLetter(s.charAt(i + 3))
                && s.charAt(i + 4) == ']') {
                
                char from = s.charAt(i + 1);
                char to = s.charAt(i + 3);
                
                result.append('[');
                if (from <= to) {
                    for (char c = from; c <= to; c++) {
                        result.append(c);
                    }
                } else {
                    result.append(s, i + 1, i + 4);
                }
                result.append(']');
                i += 5;
            } else {
                result.append(s.charAt(i));
                i++;
            }
        }

        return result.toString();
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isLowercaseLetter(char c) {
        return c >= 'a' && c <= 'z';
    }

    private static boolean isUppercaseLetter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    public static Regex compile(String regex_old) {
        String regex = process(regex_old);
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

    public boolean matches(String input) {
        if (groupCount > 0) {
            NFAEngine engine = new NFAEngine();
            return engine.matches(compiledNFA, input);
        } else {
            DFAEngine engine = new DFAEngine();
            return engine.matches(compiledDFA, input);
        }
    }

    public MatchResult match(String input) {
        if (groupCount > 0) {
            NFAEngine engine = new NFAEngine();
            return engine.match(compiledNFA, input, groupCount);
        } else {
            DFAEngine engine = new DFAEngine();
            return engine.match(compiledDFA, input);
        }
    }

    public static boolean matches(String regex, String input) {
        return compile(regex).matches(input);
    }

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

    public static String intersect(String r1, String r2) {
        Regex reg1 = compile(r1);
        Regex reg2 = compile(r2);
        DFAOperations ops = new DFAOperations();
        DFA intersection = ops.intersect(reg1.getDFA(), reg2.getDFA());

        DFAMinimizer min = new DFAMinimizer();
        DFA minInter = min.minimize(intersection);

        StateEliminator eliminator = new StateEliminator();
        String recovered = eliminator.recover(minInter);

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
