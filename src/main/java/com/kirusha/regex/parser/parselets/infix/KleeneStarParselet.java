package com.kirusha.regex.parser.parselets.infix;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.KleeneStarNode;
import com.kirusha.regex.parser.parselets.InfixParselet;

public class KleeneStarParselet implements InfixParselet {
    @Override
    public ASTNode parse(Parser parser, ASTNode left, Token token) {
        return new KleeneStarNode(left);
    }

    @Override
    public int getPrecedence() {
        return 30;
    }
}