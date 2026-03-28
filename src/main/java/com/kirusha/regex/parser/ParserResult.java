package com.kirusha.regex.parser;

import com.kirusha.regex.parser.ast.ASTNode;

public final class ParserResult {

    private final ASTNode root;
    private final int groupCount;
    private final String originalInput;

    public ParserResult(ASTNode root, int groupCount, String originalInput) {
        this.root = root;
        this.groupCount = groupCount;
        this.originalInput = originalInput;
    }

    public ASTNode getRoot() {
        return root;
    }

    public int getGroupCount() {
        return groupCount;
    }

    public String getOriginalInput() {
        return originalInput;
    }

    @Override
    public String toString() {
        return "ParserResult{" +
                "root=" + root +
                ", groupCount=" + groupCount +
                ", originalInput='" + originalInput + '\'' +
                '}';
    }
}
