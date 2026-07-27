package com.kirusha.regex.parser.parselets.prefix;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.lexer.TokenType;
import com.kirusha.regex.parser.ParserException;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.GroupNode;
import com.kirusha.regex.parser.parselets.PrefixParselet;

public class GroupParselet implements PrefixParselet {
    @Override
    public ASTNode parse(Parser parser, Token token) {
        int groupNumber = parser.nextGroupNumber();

        if (parser.peek() != null && parser.peek().getType() == TokenType.RPAREN)
            throw new ParserException("Empty group is not allowed", parser.peek().getPosition());

        ASTNode inner = parser.parseExpression(0);

        parser.expect(TokenType.RPAREN);

        return new GroupNode(groupNumber, inner);
    }
}