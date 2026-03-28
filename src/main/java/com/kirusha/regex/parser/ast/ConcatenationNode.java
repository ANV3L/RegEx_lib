package com.kirusha.regex.parser.ast;

public class ConcatenationNode extends ASTNode {

    private final ASTNode left;
    private final ASTNode right;

    public ConcatenationNode(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }

    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "Concat(" + left + ", " + right + ")";
    }
}
