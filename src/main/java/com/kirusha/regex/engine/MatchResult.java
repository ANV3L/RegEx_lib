package com.kirusha.regex.engine;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Результат операции матчинга.
 *
 * Содержит:
 * - результат проверки (matched / not matched);
 * - захваченные группы (если использовался NFA engine).
 *
 * Поддерживает:
 * - доступ к группам по индексу: group(int index);
 * - перебор групп через Iterator.
 */
public class MatchResult implements Iterable<String> {

    /**
     * Результат проверки: совпала ли строка с регулярным выражением.
     */
    private final boolean matched;

    /**
     * Список захваченных групп.
     * groups.get(0) — вся совпавшая строка (group 0);
     * groups.get(1) — первая группа захвата, и т.д.
     */
    private final List<String> groups;

    /**
     * @param matched результат проверки
     * @param groups список захваченных групп (может быть пустым)
     */
    public MatchResult(boolean matched, List<String> groups) {
        // TODO: сохранить matched и groups
        this.matched = matched;
        this.groups = groups != null
                ? Collections.unmodifiableList(groups)
                : Collections.emptyList();
    }

    /**
     * Фабричный метод для неудачного матча.
     */
    public static MatchResult noMatch() {
        return new MatchResult(false, Collections.emptyList());
    }

    /**
     * @return true если строка соответствует regex
     */
    public boolean matches() {
        return matched;
    }

    /**
     * Возвращает содержимое группы захвата по номеру.
     *
     * @param index номер группы (0 = вся строка, 1..n = группы захвата)
     * @return содержимое группы или null, если группа не участвовала в матче
     * @throws IndexOutOfBoundsException если index < 0 или index >= groupCount
     */
    public String group(int index) {
        // TODO: проверить границы и вернуть groups.get(index)
        if (index < 0 || index >= groups.size()) {
            throw new IndexOutOfBoundsException("Group index: " + index + ", count: " + groups.size());
        }
        return groups.get(index);
    }

    /**
     * @return количество групп (включая группу 0)
     */
    public int groupCount() {
        return groups.size();
    }

    /**
     * Итератор для перебора всех групп.
     */
    @Override
    public Iterator<String> iterator() {
        return groups.iterator();
    }

    @Override
    public String toString() {
        return "MatchResult{matched=" + matched + ", groups=" + groups + "}";
    }
}
