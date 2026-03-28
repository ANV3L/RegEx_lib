package com.kirusha.regex.parser.ast;

public class LiteralNode extends ASTNode {

    private final String value;

    public LiteralNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Literal(" + value + ")";
    }
}
