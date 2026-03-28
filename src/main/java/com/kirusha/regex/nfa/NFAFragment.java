package com.kirusha.regex.nfa;

/**
 * Промежуточный строительный блок для конструкции Томпсона.
 *
 * Идея:
 * каждый узел AST преобразуется не сразу в полный NFA,
 * а в маленький фрагмент:
 *
 *   start ---> ... ---> accept
 *
 * Затем эти фрагменты соединяются между собой:
 * - для конкатенации;
 * - для альтернации;
 * - для звезды Клини;
 * - для повторов.
 *
 * В результате финальный AST-корень превращается в один большой фрагмент,
 * который затем оборачивается в объект NFA.
 */
public class NFAFragment {

    /**
     * Начальное состояние фрагмента.
     */
    private final NFAState start;

    /**
     * Принимающее состояние фрагмента.
     */
    private final NFAState accept;

    /**
     * @param start стартовое состояние фрагмента
     * @param accept принимающее состояние фрагмента
     */
    public NFAFragment(NFAState start, NFAState accept) {
        this.start = start;
        this.accept = accept;
    }

    /**
     * @return стартовое состояние фрагмента
     */
    public NFAState getStart() {
        return start;
    }

    /**
     * @return принимающее состояние фрагмента
     */
    public NFAState getAccept() {
        return accept;
    }

    @Override
    public String toString() {
        return "Fragment{start=" + start + ", accept=" + accept + "}";
    }
}
