package com.kirusha.regex.parser.ast;

public class ConcatenationNode extends BinaryNode {

    public ConcatenationNode(ASTNode left, ASTNode right) {
        super(left, right);
    }
}
