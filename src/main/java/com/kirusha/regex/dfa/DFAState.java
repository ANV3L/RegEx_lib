package com.kirusha.regex.dfa;

import java.util.Map;
import java.util.Objects;

/**
 * Состояние детерминированного конечного автомата (DFA).
 *
 * В отличие от NFA, переходы детерминированы:
 * из каждого состояния по каждому символу ровно один переход.
 *
 * Переходы хранятся как:
 *   symbol -> DFAState  (не Set, а единственное состояние)
 */
public class DFAState {

    /**
     * Уникальный идентификатор состояния.
     */
    private final int id;

    /**
     * Переходы по символам.
     * Ключ — символ, значение — целевое состояние.
     */
    private final Map<String, DFAState> transitions;

    /**
     * Является ли данное состояние принимающим.
     */
    private boolean accepting;

    /**
     * @param id уникальный идентификатор
     * @param accepting является ли состояние принимающим
     */
    public DFAState(int id, boolean accepting) {
        // TODO: сохранить id и accepting
        // TODO: инициализировать transitions (HashMap)
        this.id = id;
        this.accepting = accepting;
        this.transitions = new java.util.HashMap<>();
    }

    /**
     * Добавляет переход по символу.
     * Перезаписывает существующий переход, если он уже есть (DFA — детерминированный).
     *
     * @param symbol символ перехода
     * @param target целевое состояние
     */
    public void addTransition(String symbol, DFAState target) {
        // TODO: добавить переход в transitions
        transitions.put(symbol, target);
    }

    /**
     * Возвращает состояние, в которое ведёт переход по данному символу.
     *
     * @param symbol символ
     * @return целевое состояние или null, если перехода нет
     */
    public DFAState getTransition(String symbol) {
        // TODO: вернуть transitions.get(symbol)
        return transitions.get(symbol);
    }

    public int getId() {
        return id;
    }

    public Map<String, DFAState> getTransitions() {
        return transitions;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    @Override
    public String toString() {
        return "DFAState(" + id + (accepting ? ", accept" : "") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFAState dfaState = (DFAState) o;
        return id == dfaState.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
