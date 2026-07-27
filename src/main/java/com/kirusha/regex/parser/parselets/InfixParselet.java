package com.kirusha.regex.parser.parselets;

import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.parser.Parser;
import com.kirusha.regex.parser.ast.ASTNode;

public interface InfixParselet {
    ASTNode parse(Parser parser, ASTNode left, Token token);

    int getPrecedence();
}
