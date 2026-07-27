package com.kirusha.regex.parser.parselets.prefix;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.EpsilonNode;
import com.kirusha.regex.parser.parselets.PrefixParselet;

public class EpsilonParselet implements PrefixParselet {
    @Override
    public ASTNode parse(Parser parser, Token token) {
        return new EpsilonNode();
    }
}