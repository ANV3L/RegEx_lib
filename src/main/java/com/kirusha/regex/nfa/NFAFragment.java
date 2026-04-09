package com.kirusha.regex.nfa;


public class NFAFragment {

    private final NFAState start;

    private final NFAState accept;

    public NFAFragment(NFAState start, NFAState accept) {
        this.start = start;
        this.accept = accept;
    }

    public NFAState getStart() {
        return start;
    }

    public NFAState getAccept() {
        return accept;
    }

    @Override
    public String toString() {
        return "Fragment{start=" + start + ", accept=" + accept + "}";
    }
}
