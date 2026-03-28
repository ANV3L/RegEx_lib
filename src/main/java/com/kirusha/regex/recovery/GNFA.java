package com.kirusha.regex.recovery;

import java.util.Map;
import java.util.Set;

/**
 * Обобщённый NFA (Generalized NFA).
 *
 * В отличие от обычного NFA, метки на переходах — это регулярные выражения (строки),
 * а не отдельные символы.
 *
 * Используется как промежуточная структура в алгоритме исключения состояний
 * для восстановления regex из DFA.
 *
 * Свойства:
 * - Ровно одно стартовое состояние;
 * - Ровно одно принимающее состояние;
 * - Метка перехода — регулярное выражение (строка).
 */
public class GNFA {

    /**
     * Множество всех состояний GNFA.
     */
    private final Set<Integer> states;

    /**
     * Переходы: (from, to) → regex-метка.
     * Если перехода нет, запись отсутствует (что эквивалентно пустому множеству ∅).
     */
    private final Map<Long, String> transitions;

    /**
     * Стартовое состояние.
     */
    private final int startState;

    /**
     * Принимающее состояние.
     */
    private final int acceptState;

    /**
     * @param states множество состояний
     * @param transitions переходы (ключ = encodePair(from, to), значение = regex)
     * @param startState стартовое состояние
     * @param acceptState принимающее состояние
     */
    public GNFA(Set<Integer> states, Map<Long, String> transitions,
                int startState, int acceptState) {
        // TODO: валидация
        this.states = states;
        this.transitions = transitions;
        this.startState = startState;
        this.acceptState = acceptState;
    }

    /**
     * Кодирует пару (from, to) в long для использования как ключ Map.
     */
    public static long encodePair(int from, int to) {
        return ((long) from << 32) | (to & 0xFFFFFFFFL);
    }

    /**
     * Возвращает regex-метку перехода из from в to.
     *
     * @return regex-строка или null если перехода нет
     */
    public String getTransition(int from, int to) {
        return transitions.get(encodePair(from, to));
    }

    /**
     * Устанавливает regex-метку перехода из from в to.
     */
    public void setTransition(int from, int to, String regex) {
        // TODO: установить или обновить метку
        transitions.put(encodePair(from, to), regex);
    }

    /**
     * Удаляет переход из from в to.
     */
    public void removeTransition(int from, int to) {
        transitions.remove(encodePair(from, to));
    }

    /**
     * Удаляет состояние из GNFA (используется при исключении).
     */
    public void removeState(int state) {
        // TODO: удалить состояние из states и все связанные переходы
        states.remove(state);
        transitions.entrySet().removeIf(e -> {
            long key = e.getKey();
            int from = (int) (key >> 32);
            int to = (int) key;
            return from == state || to == state;
        });
    }

    public Set<Integer> getStates() {
        return states;
    }

    public Map<Long, String> getTransitions() {
        return transitions;
    }

    public int getStartState() {
        return startState;
    }

    public int getAcceptState() {
        return acceptState;
    }
}
