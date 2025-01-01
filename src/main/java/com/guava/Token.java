package com.guava;

import com.utils.Color;

public class Token {
    final TokenType type;
    final String lexeme;
    final int line; 
    
    Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }
    
    @Override
    public String toString() {
        return String.format(
            "%s(type: %s, lexeme: %s, line: %s)",
            Color.colorize(Color.COLOR_BLUE, "Token"),
            Color.colorize(Color.COLOR_GREEN, type),
            Color.colorize(Color.COLOR_YELLOW, lexeme),
            Color.colorize(Color.COLOR_CYAN, line)
        );
    }
    
}