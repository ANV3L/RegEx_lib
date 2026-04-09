package com.kirusha.regex.parser;

import java.util.ArrayList;
import java.util.List;

import com.kirusha.regex.lexer.LexerResult;
import com.kirusha.regex.lexer.Token;
import com.kirusha.regex.lexer.TokenType;
import com.kirusha.regex.parser.ast.ASTNode;
import com.kirusha.regex.parser.ast.AlternationNode;
import com.kirusha.regex.parser.ast.BackReferenceNode;
import com.kirusha.regex.parser.ast.CharClassNode;
import com.kirusha.regex.parser.ast.ConcatenationNode;
import com.kirusha.regex.parser.ast.EpsilonNode;
import com.kirusha.regex.parser.ast.GroupNode;
import com.kirusha.regex.parser.ast.KleeneStarNode;
import com.kirusha.regex.parser.ast.LiteralNode;
import com.kirusha.regex.parser.ast.RepeatNode;

public class Parser {

    private List<Token> tokens;
    private int current;
    private int groupCounter;
    private String originalInput;

    public ParserResult parse(LexerResult lexerResult) {
        if (lexerResult == null) {
            throw new NullPointerException("LexerResult cannot be null");
        }

        this.tokens = lexerResult.getTokens();
        this.current = 0;
        this.groupCounter = 0;
        this.originalInput = lexerResult.getOriginalInput();

        if (tokens.isEmpty()) {
            throw new ParserException("Empty input", 0);
        }

        ASTNode root = parseRegex();

        if (!isAtEnd()) {
            throw error("Unexpected token: " + peek().getType());
        }

        return new ParserResult(root, groupCounter, originalInput);
    }

    private ASTNode parseRegex() {
        return parseUnion();
    }

    private ASTNode parseUnion() {
        ASTNode left = parseConcat();

        while (match(TokenType.PIPE)) {
            if (isAtEnd() || !canStartAtom()) {
                throw error("Expected expression after '|'");
            }
            ASTNode right = parseConcat();
            left = new AlternationNode(left, right);
        }

        return left;
    }

    private ASTNode parseConcat() {
        if (!canStartAtom()) {
            throw error("Expected expression");
        }

        ASTNode left = parseRepeat();

        while (!isAtEnd() && canStartAtom()) {
            ASTNode right = parseRepeat();
            left = new ConcatenationNode(left, right);
        }

        return left;
    }

    private ASTNode parseRepeat() {
        ASTNode node = parseAtom();

        while (!isAtEnd()) {
            if (match(TokenType.STAR)) {
                node = new KleeneStarNode(node);
            } else if (match(TokenType.LBRACE)) {
                Token numberToken = expect(TokenType.NUMBER, "Expected NUMBER inside repeat");
                expect(TokenType.RBRACE, "Expected '}' after repeat count");

                int count;
                try {
                    count = Integer.parseInt(numberToken.getValue());
                } catch (NumberFormatException e) {
                    throw new ParserException("Invalid repeat count: " + numberToken.getValue(),
                            numberToken.getPosition());
                }

                node = new RepeatNode(node, count);
            } else {
                break;
            }
        }

        return node;
    }

    private ASTNode parseAtom() {
        if (isAtEnd()) {
            throw error("Expected atom but found end of input");
        }

        Token token = peek();

        switch (token.getType()) {
            case CHAR:
                advance();
                return new LiteralNode(token.getValue());

            case EPSILON:
                advance();
                return new EpsilonNode();

            case BACKREF:
                advance();
                return new BackReferenceNode(Integer.parseInt(token.getValue()));

            case LPAREN:
                return parseGroup();

            case LBRACKET:
                return parseCharClass();

            case PIPE:
                throw error("Unexpected '|'");
            case RPAREN:
                throw error("Unexpected ')'");
            case RBRACKET:
                throw error("Unexpected ']'");
            case RBRACE:
                throw error("Unexpected '}'");
            case STAR:
                throw error("Unexpected '*'");
            case LBRACE:
                throw error("Unexpected '{'");
            case NUMBER:
                advance();
                String numVal = token.getValue();
                if (numVal.length() == 1) {
                    return new LiteralNode(numVal);
                }

                ASTNode numResult = new LiteralNode(String.valueOf(numVal.charAt(0)));
                for (int ci = 1; ci < numVal.length(); ci++) {
                    numResult = new ConcatenationNode(numResult, 
                        new LiteralNode(String.valueOf(numVal.charAt(ci))));
                }
                return numResult;

            default:
                throw error("Unexpected token: " + token.getType());
        }
    }

    private ASTNode parseGroup() {
        Token lparen = expect(TokenType.LPAREN, "Expected '('");

        int groupNumber = ++groupCounter;

        if (isAtEnd()) {
            throw new ParserException("Unclosed group", lparen.getPosition());
        }

        if (peek().getType() == TokenType.RPAREN) {
            throw new ParserException("Empty group is not allowed", peek().getPosition());
        }

        ASTNode inner = parseRegex();

        expect(TokenType.RPAREN, "Expected ')' after group");

        return new GroupNode(groupNumber, inner);
    }

    private ASTNode parseCharClass() {
        expect(TokenType.LBRACKET, "Expected '['");

        List<String> symbols = new ArrayList<>();

        while (!isAtEnd() && peek().getType() != TokenType.RBRACKET) {
            Token token = advance();

            switch (token.getType()) {
                case CHAR:
                case EPSILON:
                    symbols.add(token.getValue());
                    break;
                case NUMBER:
                    for (char ch : token.getValue().toCharArray()) {
                        symbols.add(String.valueOf(ch));
                    }
                    break;
                case BACKREF:
                    symbols.add("\\" + token.getValue());
                    break;
                default:
                    throw new ParserException(
                            "Invalid token inside char class: " + token.getType(),
                            token.getPosition()
                    );
            }
        }

        expect(TokenType.RBRACKET, "Expected ']' after char class");

        return new CharClassNode(symbols);
    }

    private boolean canStartAtom() {
        if (isAtEnd()) {
            return false;
        }

        TokenType type = peek().getType();

        return type == TokenType.CHAR
                || type == TokenType.NUMBER
                || type == TokenType.EPSILON
                || type == TokenType.BACKREF
                || type == TokenType.LPAREN
                || type == TokenType.LBRACKET;
    }

    private Token peek() {
        if (isAtEnd()) {
            return null;
        }
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token advance() {
        if (isAtEnd()) {
            throw error("Unexpected end of input");
        }
        return tokens.get(current++);
    }

    private boolean match(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        if (peek().getType() != type) {
            return false;
        }

        advance();
        return true;
    }

    private Token expect(TokenType type, String message) {
        if (isAtEnd()) {
            throw error(message);
        }

        if (peek().getType() != type) {
            throw error(message + ", but found " + peek().getType());
        }

        return advance();
    }

    private ParserException error(String message) {
        if (!isAtEnd()) {
            return new ParserException(message, peek().getPosition());
        }

        if (tokens != null && !tokens.isEmpty()) {
            Token last = tokens.get(tokens.size() - 1);
            return new ParserException(message, last.getPosition());
        }

        return new ParserException(message, 0);
    }
}
