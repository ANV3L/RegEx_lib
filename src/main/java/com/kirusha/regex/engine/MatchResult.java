package com.kirusha.regex.engine;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class MatchResult implements Iterable<String> {

    private final boolean matched;

    private final List<String> groups;

    public MatchResult(boolean matched, List<String> groups) {
        this.matched = matched;
        this.groups = groups != null
                ? Collections.unmodifiableList(groups)
                : Collections.emptyList();
    }

    public static MatchResult noMatch() {
        return new MatchResult(false, Collections.emptyList());
    }

    public boolean matches() {
        return matched;
    }

    public String group(int index) {
        if (index < 0 || index >= groups.size()) {
            throw new IndexOutOfBoundsException("Group index: " + index + ", count: " + groups.size());
        }
        return groups.get(index);
    }

    public int groupCount() {
        return groups.size();
    }

    @Override
    public Iterator<String> iterator() {
        return groups.iterator();
    }

    @Override
    public String toString() {
        return "MatchResult{matched=" + matched + ", groups=" + groups + "}";
    }
}
