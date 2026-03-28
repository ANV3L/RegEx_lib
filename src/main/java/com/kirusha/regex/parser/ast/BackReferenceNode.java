package com.kirusha.regex.parser.ast;

public class BackReferenceNode extends ASTNode {

    private final int groupNumber;

    public BackReferenceNode(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    @Override
    public String toString() {
        return "BackRef(" + groupNumber + ")";
    }
}
