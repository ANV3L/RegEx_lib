package com.kirusha.regex.parser.ast;

public class RepeatNode extends ASTNode {

    private final ASTNode child;
    private final int count;

    public RepeatNode(ASTNode child, int count) {
        this.child = child;
        this.count = count;
    }

    public ASTNode getChild() {
        return child;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Repeat(" + child + ", " + count + ")";
    }
}
