package com.kirusha.regex.parser.ast;

public class GroupNode extends UnaryNode {

    private final int groupNumber;

    public GroupNode(int groupNumber, ASTNode child) {
        super(child);
        this.groupNumber = groupNumber;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    @Override
    public String toString() {
        return "Group(" + groupNumber + ", " + getChild() + ")";
    }
}
