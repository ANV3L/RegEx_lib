package com.kirusha.regex.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kirusha.regex.lexer.LexerResult;
import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.lexer.TokenType;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.parselets.InfixParselet;
import com.kirusha.regex.parser.parselets.PrefixParselet;
import com.kirusha.regex.parser.parselets.infix.AlternationParselet;
import com.kirusha.regex.parser.parselets.infix.ConcatenationParselet;
import com.kirusha.regex.parser.parselets.infix.KleeneStarParselet;
import com.kirusha.regex.parser.parselets.infix.RepeatParselet;
import com.kirusha.regex.parser.parselets.infix.XORParselet;
import com.kirusha.regex.parser.parselets.prefix.BackReferenceParselet;
import com.kirusha.regex.parser.parselets.prefix.CharClassParselet;
import com.kirusha.regex.parser.parselets.prefix.EpsilonParselet;
import com.kirusha.regex.parser.parselets.prefix.GroupParselet;
import com.kirusha.regex.parser.parselets.prefix.LiteralParselet;
import com.kirusha.regex.parser.parselets.prefix.NumberParselet;

public class Parser {
    public List<Token> tokens;
    private int current;
    private int groupCounter;
    private String originalInput;

    private final Map<TokenType, PrefixParselet> prefixParselets = new HashMap<>();
    private final Map<TokenType, InfixParselet> infixParselets = new HashMap<>();

    public Parser() {
        registerOperations();
    }

    private void registerOperations() {
        registerPrefix(TokenType.CHAR, new LiteralParselet());
        registerPrefix(TokenType.EPSILON, new EpsilonParselet());
        registerPrefix(TokenType.NUMBER, new NumberParselet());
        registerPrefix(TokenType.LBRACKET, new CharClassParselet());
        registerPrefix(TokenType.LPAREN, new GroupParselet());
        registerPrefix(TokenType.BACKREF, new BackReferenceParselet());

        registerInfix(TokenType.PIPE, new AlternationParselet());
        registerInfix(TokenType.LBRACE, new RepeatParselet());
        registerInfix(TokenType.STAR, new KleeneStarParselet());
        registerInfix(TokenType.XOR, new XORParselet());
    }

    private void registerPrefix(TokenType t, PrefixParselet p) {
        prefixParselets.put(t, p);
    }

    private void registerInfix(TokenType t, InfixParselet p) {
        infixParselets.put(t, p);
    }

    public ParserResult parse(LexerResult lexerResult) {
        if (lexerResult == null)
            throw new NullPointerException("Lexer cannot be null");

        this.tokens = lexerResult.getTokens();
        this.current = 0;
        this.groupCounter = 0;
        this.originalInput = lexerResult.getOriginalInput();

        if (tokens.isEmpty())
            throw new ParserException("Empty input", 0);

        ASTNode root = parseExpression(0);

        if (!isAtEnd())
            throw new ParserException("Unexpected token: " + peek().getType(), peek().getPosition());

        return new ParserResult(root, groupCounter, originalInput);
    }

    public ASTNode parseExpression(int precedence) {
        Token token = advance();

        PrefixParselet prefix = prefixParselets.get(token.getType());
        if (prefix == null)
            throw error("Expected expression, got: " + token.getType());
        ASTNode left = prefix.parse(this, token);

        while (precedence < getPrecedence()) {
            Token nextToken = peek();

            InfixParselet infix = infixParselets.get(nextToken.getType());

            if (infix == null && canStartAtom())
                infix = new ConcatenationParselet();

            if (infix == null || infix.getPrecedence() < precedence)
                break;

            if (!(infix instanceof ConcatenationParselet))
                advance();

            left = infix.parse(this, left, nextToken);
        }
        return left;
    }

    private int getPrecedence() {
        if (isAtEnd())
            return 0;

        InfixParselet parselet = infixParselets.get(peek().getType());

        if (parselet != null)
            return parselet.getPrecedence();

        if (canStartAtom())
            return new ConcatenationParselet().getPrecedence();

        return 0;
    }

    public boolean canStartAtom() {
        if (isAtEnd())
            return false;

        return prefixParselets.containsKey(peek().getType());
    }

    public Token peek() {
        if (isAtEnd())
            return null;

        return tokens.get(current);
    }

    public boolean isAtEnd() {
        return current >= tokens.size();
    }

    public Token advance() {
        if (isAtEnd())
            throw error("Unexpected end");

        return tokens.get(current++);
    }

    public Token expect(TokenType type) {
        if (isAtEnd())
            throw error("Expected " + type + "but reached end");

        Token token = peek();
        if (token.getType() != type)
            throw error("Expected " + type + "but found" + token.getType());

        return advance();
    }

    public int nextGroupNumber() {
        return ++groupCounter;
    }

    public ParserException error(String message) {
        if (!isAtEnd())
            return new ParserException(message, peek().getPosition());

        if (tokens != null && !tokens.isEmpty()) {
            Token last = tokens.get(tokens.size() - 1);
            return new ParserException(message, last.getPosition());
        }

        return new ParserException(message, 0);
    }

}