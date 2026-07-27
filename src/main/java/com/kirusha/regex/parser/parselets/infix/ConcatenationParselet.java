package com.kirusha.regex.parser.parselets.infix;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.ConcatenationNode;
import com.kirusha.regex.parser.parselets.InfixParselet;

public class ConcatenationParselet implements InfixParselet {
    @Override
    public ASTNode parse(Parser parser, ASTNode left, Token token) {

        ASTNode right = parser.parseExpression(getPrecedence() + 1);

        return new ConcatenationNode(left, right);
    }

    @Override
    public int getPrecedence() {
        return 2000;
    }
}