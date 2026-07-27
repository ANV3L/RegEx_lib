package com.kirusha.regex.parser.ast;

public abstract class UnaryNode extends ASTNode {
    private final ASTNode child;

    protected UnaryNode(ASTNode child) {
        this.child = child;
    }

    public ASTNode getChild() {
        return child;
    }

    @Override
    public String toString() {
        String className = getClass().getSimpleName();
        String name = className.replace("Node", "");
        return name + "(" + child + ")";
    }
}