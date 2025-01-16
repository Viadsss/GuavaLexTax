package com.guava;

import com.utils.Color;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line; 
    
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }
    
    @Override
    public String toString() {
        return String.format(
            "%s(type: %s, lexeme: %s, literal: %s, line: %s)",
            Color.colorize(Color.COLOR_BLUE, "Token"),
            Color.colorize(Color.COLOR_GREEN, type),
            Color.colorize(Color.COLOR_YELLOW, lexeme),
            Color.colorize(Color.COLOR_PURPLE, literal),
            Color.colorize(Color.COLOR_CYAN, line)
        );
    }
    
}