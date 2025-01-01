package com.guava;

import static com.guava.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private static final Map<String, TokenType> keywords;
    
    static {
        keywords = new HashMap<>();
        keywords.put("Comp",  COMP);
        keywords.put("func",   FUNC);
        keywords.put("if",     IF);
        keywords.put("else",   ELSE);
        keywords.put("true",   TRUE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("do",     DO);
        keywords.put("while",  WHILE);
        keywords.put("return", RETURN);
        keywords.put("print",  PRINT);
        keywords.put("read",   READ);
        keywords.put("null",   NULL);        
        keywords.put("var",    VAR);
        keywords.put("expose", EXPOSE);

        // Special Units
        keywords.put("px", PX);
        keywords.put("em", EM);
        keywords.put("percent", PERCENT);

        // Component Supporting Keywords
        keywords.put("layout", LAYOUT);
        keywords.put("style", STYLE);
        keywords.put("event", EVENT);
        keywords.put("bind", BIND);
        keywords.put("add", ADD);
        keywords.put("flex", FLEX);
        keywords.put("grid", GRID);
        keywords.put("absolute", ABSOLUTE);

        // Component Types Keywords
        keywords.put("frame", FRAME);        
        keywords.put("panel", PANEL);        
        keywords.put("label", LABEL);        
        keywords.put("button", BUTTON);        
        keywords.put("textField", TEXTFIELD);        
        keywords.put("passwordField", PASSWORDFIELD);        
    }    
    
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;    
    
    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Ignore whitespaces
            case ' ':
            case '\r':
            case '\t':                          break;
            case '\n': line++;                  break;
            case '"': string();                 break;            
            // Single
            case '(': addToken(LEFT_PAREN);     break;
            case ')': addToken(RIGHT_PAREN);    break;
            case ',': addToken(COMMA);          break;
            case '.': addToken(DOT);            break;
            case ';': addToken(SEMICOLON);      break;
            case ':': addToken(COLON);          break;
            case '{': addToken(LEFT_BRACE);     break;
            
            // Possible doubles
            case '}': addToken(match('@') ? AT_RBRACE : RIGHT_BRACE);
            break;
            case '@': addToken(match('{') ? AT_LBRACE : AT);
            break;             
            case '!':
            addToken(match('=') ? BANG_EQUAL : BANG);
            break;
            case '=':
            addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            break;
            case '<':
            addToken(match('=') ? LESS_EQUAL : LESS);
            break;
            case '>':
            addToken(match('=') ? GREATER_EQUAL : GREATER);
            break;
            case '+':
                if (match('+')) addToken(INCREMENT);
                else if (match('=')) addToken(PLUS_EQUAL);
                else addToken(PLUS);
                break;
            case '-':
                if (match('-')) addToken(DECREMENT);
                else if (match('=')) addToken(MINUS_EQUAL);
                else addToken(MINUS);
                break;
            case '%':
                addToken(match('=') ? MODULO_EQUAL : MODULO);
                break;
            case '*':
                addToken(match('=') ? STAR_EQUAL : STAR);
                break;
            case '/':
                if (match('=')) addToken(SLASH_EQUAL);
                else if (match('/')) {
                    // A comment that goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance(); 
                } else if (match('*')) {
                    blockComment();
                } else addToken(SLASH);
                break;
            case '&': addToken(match('&') ? AND : AMPERSAND);
                break;
            case '|': addToken(match('|') ? OR : PIPE);
                break;
            default:
            if (isDigit(c)) number();
            else if (isAlpha(c)) identifier();
            else Guava.error(line, "Unexpected character.");
        }
    }

    private void blockComment() {
        while (peek() != '*' && !isAtEnd()) {
            if (peek() == '\n') line++; 
            advance(); 
        }


        if (isAtEnd()) {
            Guava.error(line, "Unterminated multi-line comment.");
            return;
        }

        advance();

        // Look for the closing "*/"
        while (peek() != '/' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance(); 
        }

        if (isAtEnd()) {
            Guava.error(line, "Unterminated multi-line comment.");
            return;
        }

        advance();
    }    

    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }
    
    private void number() {
        while (isDigit(peek())) advance();
        
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            
            while (isDigit(peek())) advance();
        }
        
        addToken(NUMBER);
    }
    
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        
        if (isAtEnd()) {
            Guava.error(line, "Unterminated string.");
            return;
        }
        
        advance();
        
        addToken(STRING);
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        return true;
    }    

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }    
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }    

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, line));
    }
}
