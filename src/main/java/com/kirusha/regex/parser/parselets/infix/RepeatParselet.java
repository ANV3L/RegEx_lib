package com.kirusha.regex.parser.parselets.infix;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.lexer.TokenType;
import com.kirusha.regex.parser.ParserException;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.RepeatNode;
import com.kirusha.regex.parser.parselets.InfixParselet;

public class RepeatParselet implements InfixParselet {
    @Override
    public ASTNode parse(Parser parser, ASTNode left, Token token) {

        Token numberToken = parser.expect(TokenType.NUMBER);

        int count;
        try {
            count = Integer.parseInt(numberToken.getValue());
        } catch (NumberFormatException e) {
            throw new ParserException(
                    "Invalid repeat count: " + numberToken.getValue(),
                    numberToken.getPosition());
        }

        parser.expect(TokenType.RBRACE);

        return new RepeatNode(left, count);
    }

    @Override
    public int getPrecedence() {
        return 30;
    }
}