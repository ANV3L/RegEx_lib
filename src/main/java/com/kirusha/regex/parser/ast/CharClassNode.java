package com.kirusha.regex.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharClassNode extends ASTNode {

    private final List<String> symbols;

    public CharClassNode(List<String> symbols) {
        this.symbols = Collections.unmodifiableList(new ArrayList<>(symbols));
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public boolean isEmpty() {
        return symbols.isEmpty();
    }

    @Override
    public String toString() {
        return "CharClass(" + symbols + ")";
    }
}
