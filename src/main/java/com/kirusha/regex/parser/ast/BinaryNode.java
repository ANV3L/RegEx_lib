package com.kirusha.regex.parser.ast;

public abstract class BinaryNode extends ASTNode {
    private final ASTNode left;
    private final ASTNode right;

    protected BinaryNode(ASTNode left, ASTNode right) {
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
        String className = getClass().getSimpleName();
        String name = className.replace("Node", "");
        return name + "(" + left + ", " + right + ")";
    }
}