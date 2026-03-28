package com.kirusha.regex.parser.ast;

public class KleeneStarNode extends ASTNode {

    private final ASTNode child;

    public KleeneStarNode(ASTNode child) {
        this.child = child;
    }

    public ASTNode getChild() {
        return child;
    }

    @Override
    public String toString() {
        return "Star(" + child + ")";
    }
}
