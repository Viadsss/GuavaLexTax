package com.guava;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.*;
import java.awt.Color;
import java.util.List;

public class GuavaSyntaxHighlighter extends DocumentFilter {
    private static final StyleContext styleContext = StyleContext.getDefaultStyleContext();
    private final JTextPane textPane;
    private boolean updating = false;
    private Timer debounceTimer;
    
    // Light theme colors
    private Color COMP_COLOR = new Color(223, 142, 29); // Yellow
    private Color COMP_KEYWORD_COLOR = new Color(234, 118, 203); // Pink
    private Color KEYWORD_COLOR = new Color(136, 57, 239); // Mauve
    private Color GROUPING_COLOR = new Color(254, 100, 11); // Peach
    private Color LITERAL_COLOR = new Color(234, 118, 203); // Flamingo
    private Color OPERATOR_COLOR = new Color(23, 146, 15); // Teal
    private Color STRING_COLOR = new Color(64, 160, 43); // Green
    private Color NUMBER_COLOR = new Color(254, 100, 11); // Peach
    private Color IDENTIFIER_COLOR = new Color(0, 0, 0); // Text try 0, 0, 0
    
    private AttributeSet KEYWORD_ATTR = getStyle(KEYWORD_COLOR);
    private AttributeSet LITERAL_ATTR = getStyle(LITERAL_COLOR);
    private AttributeSet OPERATOR_ATTR = getStyle(OPERATOR_COLOR);
    private AttributeSet STRING_ATTR = getStyle(STRING_COLOR);
    private AttributeSet NUMBER_ATTR = getStyle(NUMBER_COLOR);
    private AttributeSet IDENTIFIER_ATTR = getStyle(IDENTIFIER_COLOR);
    private AttributeSet COMP_ATTR = getStyle(COMP_COLOR);
    private AttributeSet COMP_KEYWORD_ATTR = getStyle(COMP_KEYWORD_COLOR);
    private AttributeSet GROUPING_ATTR = getStyle(GROUPING_COLOR);

    
    public GuavaSyntaxHighlighter(JTextPane textPane) {
        this.textPane = textPane;
        ((AbstractDocument) textPane.getDocument()).setDocumentFilter(this);
    }
    
    @Override
    public void insertString(FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {
        if (!updating) {
            super.insertString(fb, offset, str, attr);
            handleTextChange();
        }
    }
    
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        if (!updating) {
            super.remove(fb, offset, length);
            handleTextChange();
        }
    }
    
    @Override
    public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
        if (!updating) {
            super.replace(fb, offset, length, str, attrs);
            handleTextChange();
        }
    }
    
private void handleTextChange() {
    if (debounceTimer != null) {
        debounceTimer.stop();
    }
    debounceTimer = new Timer(100, e -> {
        debounceTimer.stop();
        SwingUtilities.invokeLater(() -> {
            if (!updating) {
                updating = true;
                try {
                    String source = textPane.getText();
                    Lexer lexer = new Lexer(source);
                    List<Token> tokens = lexer.scanTokens();

                    applyHighlighting(tokens);
                } finally {
                    updating = false;
                }
            }
        });
    });
    debounceTimer.setRepeats(false);
    debounceTimer.start();
}

    
    
    private void applyHighlighting(List<Token> tokens) {
        StyledDocument doc = textPane.getStyledDocument();
        int lastIndex = 0;
        
        for (Token token : tokens) {
            try {
                String text = doc.getText(lastIndex, doc.getLength() - lastIndex);
                int start = text.indexOf(token.lexeme);
                if (start == -1) continue;
                
                start += lastIndex;
                int length = token.lexeme.length();
                lastIndex = start + length;
                
                doc.setCharacterAttributes(start, length, getAttributeForToken(token.type), true);
            } catch (BadLocationException e) {
                break;
            }
        }
    }
    
    private static AttributeSet getStyle(Color color) {
        return styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, color);
    }
    
    private AttributeSet getAttributeForToken(TokenType type) {
        if (isKeyword(type)) return KEYWORD_ATTR;
        if (isCompKeyword(type)) return COMP_KEYWORD_ATTR;
        if (isGrouping(type)) return GROUPING_ATTR;
        if (isLiteral(type)) return LITERAL_ATTR;
        if (isOperator(type)) return OPERATOR_ATTR;
        
        switch (type) {
            case COMP: return COMP_ATTR;
            case STRING: return STRING_ATTR;
            case NUMBER: return NUMBER_ATTR;
            case IDENTIFIER: return IDENTIFIER_ATTR;
            default: return styleContext.getEmptySet();
        }

    }
    
    private static boolean isKeyword(TokenType type) {
        switch (type) {
            case FUNC: case IF: case ELSE: case FOR: case DO: case WHILE:
            case RETURN: case PRINT: case READ: case VAR: case EXPOSE:
            case LAYOUT: case STYLE: case EVENT: case BIND:
            case PX: case EM: case PERCENT:
                return true;
            default:
                return false;
        }
    }

    private static boolean isCompKeyword(TokenType type) {
        switch (type) {
            case LAYOUT: case STYLE: case EVENT: case BIND:
            case ADD: case FLEX: case GRID: case ABSOLUTE: case FRAME:
            case PANEL: case LABEL: case BUTTON: case TEXTFIELD:
            case PASSWORDFIELD:
                return true;
            default:
                return false;

        }
    }
    
    private static boolean isGrouping(TokenType type) {
        switch (type) {
            case LEFT_PAREN: case RIGHT_PAREN:
            case LEFT_BRACE: case RIGHT_BRACE:
            case AT_LBRACE: case AT_RBRACE: 
                return true;
            default:
                return false;
        }
    }

    
    private static boolean isLiteral(TokenType type) {
        return type == TokenType.TRUE || type == TokenType.FALSE || type == TokenType.NULL;
    }
    
    private static boolean isOperator(TokenType type) {
        switch (type) {
            case PLUS: case MINUS: case STAR: case SLASH: case MODULO:
            case EQUAL: case BANG: case BANG_EQUAL: case EQUAL_EQUAL:
            case GREATER: case GREATER_EQUAL: case LESS: case LESS_EQUAL:
            case PLUS_EQUAL: case MINUS_EQUAL: case STAR_EQUAL: case SLASH_EQUAL: case MODULO_EQUAL:
            case OR: case AND: case INCREMENT: case DECREMENT:
                return true;
            default:
                return false;
        }
    }
}