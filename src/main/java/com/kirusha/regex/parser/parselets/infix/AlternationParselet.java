package com.kirusha.regex.parser.parselets.infix;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.AlternationNode;
import com.kirusha.regex.parser.parselets.InfixParselet;

public class AlternationParselet implements InfixParselet {
    @Override
    public ASTNode parse(Parser parser, ASTNode left, Token token) {
        if (parser.isAtEnd() || !parser.canStartAtom()) {
            throw parser.error("Expected expression after '|'");
        }

        ASTNode right = parser.parseExpression(getPrecedence());

        return new AlternationNode(left, right);
    }

    @Override
    public int getPrecedence() {
        return 10;
    }
}