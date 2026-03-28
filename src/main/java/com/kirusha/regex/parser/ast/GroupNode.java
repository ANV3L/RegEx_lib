package com.kirusha.regex.parser.ast;

public class GroupNode extends ASTNode {

    private final int groupNumber;
    private final ASTNode child;

    public GroupNode(int groupNumber, ASTNode child) {
        this.groupNumber = groupNumber;
        this.child = child;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public ASTNode getChild() {
        return child;
    }

    @Override
    public String toString() {
        return "Group(" + groupNumber + ", " + child + ")";
    }
}
