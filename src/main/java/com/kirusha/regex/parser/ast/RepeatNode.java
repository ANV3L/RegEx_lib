package com.kirusha.regex.parser.ast;

public class RepeatNode extends UnaryNode {

    private final int count;

    public RepeatNode(ASTNode child, int count) {
        super(child);
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Repeat(" + getChild() + ", " + count + ")";
    }
}
