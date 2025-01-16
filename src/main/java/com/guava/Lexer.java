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
        keywords.put("main",   MAIN);
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
        keywords.put("expose", EXPOSE);
        keywords.put("break", BREAK);
        keywords.put("continue", CONTINUE);
        
        // Data Types
        keywords.put("int",    INT);
        keywords.put("float",  FLOAT);
        keywords.put("double", DOUBLE);
        keywords.put("char",   CHAR);
        keywords.put("string", STRING);
        keywords.put("bool",   BOOL);
        
        // + Return type
        keywords.put("void",   VOID);

        // Built-in Class Keywords
        keywords.put("Comp",   COMP);
        keywords.put("Style",  STYLE);
        keywords.put("Event",  EVENT);

        // Component Method Keywords
        keywords.put("add",    ADD);
        
        // Component Types Keywords
        keywords.put("frame",  FRAME);
        keywords.put("panel",  PANEL);
        keywords.put("label",  LABEL);
        keywords.put("button", BUTTON);
        keywords.put("field", FIELD);
        keywords.put("dialog", DIALOG);
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
        
        tokens.add(new Token(EOF, "", null, line));
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
            case '\"': string();                 break;
            case '\'': character();              break;
            
            // Single-character tokens
            case '(': addToken(LEFT_PAREN);       break;
            case ')': addToken(RIGHT_PAREN);      break;
            case '{': addToken(LEFT_CBRACE);      break;
            case '}': addToken(RIGHT_CBRACE);     break;
            case ',': addToken(COMMA);            break;
            case '.': addToken(DOT);              break;
            case ';': addToken(SEMICOLON);        break;
            case ':': addToken(COLON);            break;
            
            // Operators that might be followed by '='
            case '!': addToken(match('=') ? BANG_EQUAL : BANG);       break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL);     break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS);       break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '+': 
            if (match('+')) addToken(INCREMENT);
            else if (match('=')) addToken(PLUS_EQUAL);
            else addToken(PLUS);
            break;
            case '-':
            if (match('-')) addToken(DECREMENT);
            else if (match('=')) addToken(MINUS_EQUAL);
            else if (match('>')) addToken(RIGHT_ARROW);
            else addToken(MINUS);
            break;
            case '*': addToken(match('=') ? STAR_EQUAL : STAR); break;
            case '/': 
            if (match('=')) addToken(SLASH_EQUAL);
            else if (match('/')) {
                // Single-line comment
                while (peek() != '\n' && !isAtEnd()) advance();
            } else if (match('*')) {
                blockComment();
            } else {
                addToken(SLASH);
            }
            break;
            case '%': addToken(match('=') ? MODULO_EQUAL : MODULO); break;
            
            // Logical operators
            case '&': if (match('&')) addToken(AND); break;
            case '|': if (match('|')) addToken(OR); break;            
            
            default:
            if (isDigit(c)) number();
            else if (isAlpha(c)) identifier();
            else Guava.error(line, "Unexpected character.");
            break;
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

        // Look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // Consume the "."
            while (isDigit(peek())) advance();
            if (peek() == 'f' || peek() == 'F') {
                advance();
                addToken(FLOAT_LITERAL, Float.parseFloat(source.substring(start, current)));
            } else {
                addToken(DOUBLE_LITERAL, Double.parseDouble(source.substring(start, current)));
            }

        } else {
            addToken(INTEGER_LITERAL, Integer.parseInt(source.substring(start, current)));
        }
    }    
    
    
    private void character() {
        if (isAtEnd() || peek() == '\0') {
            Guava.error(line, "Unterminated character literal.");
            return;
        }

        char next = peek();
        if (next == '\'') {
            advance();
            Guava.error(line, "Empty character literal.");
            return;
        }
        
        char value; // To store the final character value.
        
        if (next == '\\') {
            advance(); // Consume backslash
            next = peek();
            switch (next) {
                case 'n': value = '\n'; break;
                case 't': value = '\t'; break;
                case 'r': value = '\r'; break;
                case 'b': value = '\b'; break;
                case 'f': value = '\f'; break;
                case '\\': value = '\\'; break;
                case '\'': value = '\''; break;
                case '"': value = '"'; break;
                default: {
                    Guava.error(line, "Invalid escape sequence \\" + next);
                    return;
                }
            }
            advance(); 
        } else {
            value = next; 
            advance(); 
        }

        if (peek() != '\'') {
            Guava.error(line, "Invalid character literal.");
            return;
        }
        advance(); // Consume closing quote
        

        addToken(CHAR_LITERAL, value);
    }

    private void string() {
        int stringLine = line;

        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            if (peek() == '\\') advance(); // Skip escape character
            advance();
        }

        if (isAtEnd()) {
            Guava.error(stringLine, "Unterminated string.");
            return;
        }

        advance(); // The closing "
        
        // Trim the quotes and process escape sequences
        String value = source.substring(start + 1, current - 1);
        addToken(STRING_LITERAL, processEscapeSequences(value));
    }


    // Helper methods
    private String processEscapeSequences(String value) {
        StringBuilder processed = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 'n': processed.append('\n'); break;
                    case 't': processed.append('\t'); break;
                    case 'r': processed.append('\r'); break;
                    case 'b': processed.append('\b'); break;
                    case 'f': processed.append('\f'); break;
                    case '\\': processed.append('\\'); break;
                    case '"': processed.append('"'); break;
                    case '\'': processed.append('\''); break;
                    default:
                        Guava.error(line, "Invalid escape sequence \\" + next);
                        return value;
                }
            } else {
                processed.append(value.charAt(i));
            }
        }
        return processed.toString();
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
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}

