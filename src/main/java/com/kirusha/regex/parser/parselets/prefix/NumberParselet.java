package com.kirusha.regex.parser.parselets.prefix;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.ConcatenationNode;
import com.kirusha.regex.parser.ast.LiteralNode;
import com.kirusha.regex.parser.parselets.PrefixParselet;

public class NumberParselet implements PrefixParselet {
    @Override
    public ASTNode parse(Parser parser, Token token) {
        String numVal = token.getValue();

        if (numVal.length() == 1) {
            return new LiteralNode(numVal);
        }

        ASTNode result = new LiteralNode(String.valueOf(numVal.charAt(0)));

        for (int i = 1; i < numVal.length(); i++) {
            result = new ConcatenationNode(
                    result,
                    new LiteralNode(String.valueOf(numVal.charAt(i))));
        }

        return result;
    }
}