package com.kirusha.regex.parser.parselets.prefix;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.LiteralNode;
import com.kirusha.regex.parser.parselets.PrefixParselet;

public class LiteralParselet implements PrefixParselet {
    @Override
    public ASTNode parse(Parser parser, Token token) {
        return new LiteralNode(token.getValue());
    }
}