package com.guava;

public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, SEMICOLON, COLON,
    COMMA, DOT, PLUS, MINUS, STAR, SLASH, MODULO, AT, AMPERSAND, PIPE,
    
    // One 
    BANG, EQUAL,
    GREATER, LESS,

    // two character tokens.
    BANG_EQUAL, EQUAL_EQUAL,
    GREATER_EQUAL, LESS_EQUAL,
    OR, AND,
    AT_LBRACE, AT_RBRACE,
    
    PLUS_EQUAL, MINUS_EQUAL,
    STAR_EQUAL, SLASH_EQUAL,
    MODULO_EQUAL,

    INCREMENT, DECREMENT,

    // Literals.
    IDENTIFIER, STRING, NUMBER, 
    
    // Keywords.
    FUNC, IF, ELSE, TRUE, FALSE, FOR, DO, WHILE,
    RETURN, PRINT, READ, NULL, VAR, EXPOSE,
    
    // Special Unit keywords
    PX, EM, PERCENT,
    
    // Additional Keywords
    COMP, LAYOUT, STYLE, EVENT, BIND, ADD,
    FLEX, GRID, ABSOLUTE,

    // Component Types Keywords
    FRAME, PANEL, LABEL, BUTTON, TEXTFIELD, PASSWORDFIELD,

    // Not still sure
    // MAIN,

    EOF,
}


// public enum TokenType {
//     // Single-character tokens.
//     SEMICOLON, COLON,
//      AT, AMPERSAND, PIPE,
  