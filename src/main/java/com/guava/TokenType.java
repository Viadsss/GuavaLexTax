package com.guava;

public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_CBRACE, RIGHT_CBRACE,
    SEMICOLON, COLON, COMMA, DOT, 
    
    // One character tokens with possible second character
    BANG, EQUAL,
    GREATER, LESS,
    PLUS, MINUS, STAR, SLASH, MODULO,    

    // Two character tokens.
    BANG_EQUAL, EQUAL_EQUAL,
    GREATER_EQUAL, LESS_EQUAL,
    OR, AND,
    RIGHT_ARROW,
    
    PLUS_EQUAL, MINUS_EQUAL,
    STAR_EQUAL, SLASH_EQUAL,
    MODULO_EQUAL,

    INCREMENT, DECREMENT,

    // Literals
    IDENTIFIER, 
    STRING_LITERAL,   // for string literals like "foo\n bar" 
    CHAR_LITERAL,     // For character literals like 'a', '\n'
    INTEGER_LITERAL,  // For integers like 42
    FLOAT_LITERAL,   // For floating point like 3.14
    DOUBLE_LITERAL,   // For double precision like 99999.999999
    
    // Keywords.
    MAIN, IF, ELSE, TRUE, FALSE, FOR, DO, WHILE,
    RETURN, PRINT, READ, NULL, EXPOSE, BREAK, CONTINUE,

    // Type Keywords
    VOID, INT, FLOAT, DOUBLE,  
    STRING, CHAR, BOOL,    

    // Native Class Type Keywords
    COMP, STYLE, EVENT,

    // Component Method Keywords
    ADD,

    // Component Types Keywords
    FRAME, PANEL, LABEL, BUTTON, FIELD, DIALOG,  

    EOF
}