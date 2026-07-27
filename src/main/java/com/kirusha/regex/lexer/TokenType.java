package com.kirusha.regex.lexer;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {

    BadToken,

    // Обычный символ алфавита (a, b, c...) или экранированный метасимвол (\*, \|,
    // ...)
    CHAR,

    // Число внутри фигурных скобок для повтора {n}
    NUMBER,

    // Ссылка на группу захвата: \1, \2 ... \9
    BACKREF,

    // Символ '|' — операция "или" (альтернация)
    PIPE('|'),

    // Символ '*' — замыкание Клини (ноль или более повторений)
    STAR('*'),

    // Символ '(' — открывающая скобка (группа захвата или приоритет)
    LPAREN('('),

    // Символ ')' — закрывающая скобка
    RPAREN(')'),

    // Символ '[' — начало набора символов
    LBRACKET('['),

    // Символ ']' — конец набора символов
    RBRACKET(']'),

    // Символ '{' — начало повтора
    LBRACE('{'),

    // Символ '}' — конец повтора
    RBRACE('}'),

    // Символ '~' — пустая подстрока (эпсилон)
    EPSILON('~'),

    // Символ '#' - XOR
    XOR('#'),

    ;

    private final Character symbol;
    private static final Map<Character, TokenType> TOKENS = new HashMap<>();

    TokenType() {
        this.symbol = null;
    }

    TokenType(char symbol) {
        this.symbol = symbol;
    }

    static {
        for (TokenType type : values()) {
            if (type.symbol != null) {
                TOKENS.put(type.symbol, type);
            }
        }
    }

    public static TokenType getToken(char c) {
        return TOKENS.getOrDefault(c, BadToken);
    }

}