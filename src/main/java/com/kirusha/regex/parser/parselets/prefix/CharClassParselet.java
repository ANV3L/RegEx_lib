package com.kirusha.regex.parser.parselets.prefix;

import java.util.ArrayList;
import java.util.List;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.lexer.TokenType;
import com.kirusha.regex.parser.ParserException;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.CharClassNode;
import com.kirusha.regex.parser.parselets.PrefixParselet;

public class CharClassParselet implements PrefixParselet {
    @Override
    public ASTNode parse(Parser parser, Token token) {
        List<String> symbols = new ArrayList<>();

        while (parser.peek() != null && parser.peek().getType() != TokenType.RBRACKET) {
            Token current = parser.advance();

            switch (current.getType()) {
                case CHAR:
                case EPSILON:
                    symbols.add(current.getValue());
                    break;

                case NUMBER:
                    for (char ch : current.getValue().toCharArray()) {
                        symbols.add(String.valueOf(ch));
                    }
                    break;

                case BACKREF:
                    symbols.add("\\" + current.getValue());
                    break;

                default:
                    throw new ParserException(
                            "Invalid token inside char class: " + current.getType(),
                            current.getPosition());
            }
        }

        parser.expect(TokenType.RBRACKET);

        return new CharClassNode(symbols);
    }
}