package com.kirusha.regex.nfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Состояние недетерминированного конечного автомата (NFA).
 *
 * Каждое состояние должно иметь:
 * - уникальный идентификатор;
 * - переходы по символам;
 * - epsilon-переходы.
 *
 * Особенность NFA:
 * из одного состояния по одному символу может быть
 * несколько переходов сразу.
 *
 * Поэтому переходы по символам хранятся не как:
 *   char -> state
 * а как:
 *   char -> Set<NFAState>
 *
 * Epsilon-переходы хранятся отдельно.
 */
public class NFAState {

    /**
     * Уникальный идентификатор состояния.
     * Полезен для отладки, печати автомата и тестов.
     */
    private final int id;

    /**
     * Переходы по обычным символам.
     *
     * Пример:
     *   transitions.get('a') -> множество состояний,
     *   в которые можно перейти по символу 'a'
     */
    private final Map<String, Set<NFAState>> transitions;

    /**
     * Epsilon-переходы.
     * Это переходы, которые не потребляют символ входной строки.
     */
    private final Set<NFAState> epsilonTransitions;

    /**
     * Метка открытия группы захвата.
     */
    private int groupOpen = -1;

    /**
     * Метка закрытия группы захвата.
     */
    private int groupClose = -1;

    /**
     * Переходы по обратным ссылкам.
     */
    private final Map<Integer, NFAState> backrefTransitions;

    /**
     * Должен создать состояние с заданным id
     * и инициализировать пустые коллекции переходов.
     *
     * @param id идентификатор состояния
     */
    public NFAState(int id) {
        this.id = id;
        this.transitions = new HashMap<>();
        this.epsilonTransitions = new HashSet<>();
        this.backrefTransitions = new HashMap<>();
    }

    /**
     * Добавляет переход по символу из текущего состояния в target.
     *
     * Если по данному символу уже есть переходы,
     * target должен быть добавлен в множество.
     *
     * @param symbol символ перехода
     * @param target целевое состояние
     */
    public void addTransition(String symbol, NFAState target) {
        transitions.computeIfAbsent(symbol, k -> new HashSet<>()).add(target);
    }

    /**
     * Добавляет epsilon-переход из текущего состояния в target.
     *
     * @param target целевое состояние
     */
    public void addEpsilonTransition(NFAState target) {
        epsilonTransitions.add(target);
    }

    /**
     * Добавляет переход по обратной ссылке.
     */
    public void addBackrefTransition(int group, NFAState target) {
        backrefTransitions.put(group, target);
    }

    public int getGroupOpen() {
        return groupOpen;
    }

    public void setGroupOpen(int groupOpen) {
        this.groupOpen = groupOpen;
    }

    public int getGroupClose() {
        return groupClose;
    }

    public void setGroupClose(int groupClose) {
        this.groupClose = groupClose;
    }

    public Map<Integer, NFAState> getBackrefTransitions() {
        return backrefTransitions;
    }

    /**
     * @return id состояния
     */
    public int getId() {
        return id;
    }

    /**
     * @return все символьные переходы
     */
    public Map<String, Set<NFAState>> getTransitions() {
        return transitions;
    }

    /**
     * @return epsilon-переходы
     */
    public Set<NFAState> getEpsilonTransitions() {
        return epsilonTransitions;
    }

    /**
     * Удобное строковое представление состояния.
     *
     * Например:
     *   State(3)
     */
    @Override
    public String toString() {
        return "State(" + id + ")";
    }

    /**
     * Для корректной работы в Set/Map обычно удобно
     * определять equals/hashCode по id.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NFAState nfaState = (NFAState) o;
        return id == nfaState.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
