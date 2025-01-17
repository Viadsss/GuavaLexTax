package com.guava;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntaxHighlighter extends DefaultStyledDocument {
    private final StyleContext styleContext = StyleContext.getDefaultStyleContext();
    private final Map<TokenType, AttributeSet> tokenStyles;
    private AttributeSet defaultStyle;
    private boolean isProcessing = false;
    private List<Token> tokens;
    
    public SyntaxHighlighter() {
        // Initialize styles for different token types
        tokenStyles = new HashMap<>();
        
        // Load styles from the ThemeManager
        applyTheme();
        
        // Add document listener for delayed updating
        addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processChangedText();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                processChangedText();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not used for plain text
            }
        });
    }
    
    private void applyTheme() {
        Map<String, Color> theme = ThemeManager.getCurrentTheme();
        
        Color defaultTextColor = theme.getOrDefault("default", Color.BLACK);
        defaultStyle = styleContext.addAttribute(styleContext.getEmptySet(),
        StyleConstants.Foreground, defaultTextColor);
        
        // Control flow keywords
        addStyles(theme.get("keyword"), 
        TokenType.MAIN, TokenType.IF, TokenType.ELSE, 
        TokenType.FOR, TokenType.DO, TokenType.WHILE, 
        TokenType.RETURN, TokenType.PRINT, TokenType.READ,
        TokenType.EXPOSE, TokenType.BREAK, TokenType.CONTINUE);
        
        // Types
        addStyles(theme.get("type"), 
        TokenType.VOID, TokenType.INT, TokenType.FLOAT, 
        TokenType.DOUBLE, TokenType.STRING, TokenType.CHAR, 
        TokenType.BOOL, TokenType.COMP, TokenType.STYLE, 
        TokenType.EVENT);
        
        // Component keywords
        addStyles(theme.get("component"), 
        TokenType.FRAME, TokenType.PANEL, TokenType.LABEL, 
        TokenType.BUTTON, TokenType.FIELD, TokenType.DIALOG, 
        TokenType.ADD);
        
        // String literals
        addStyles(theme.get("string"), 
        TokenType.STRING_LITERAL);
        
        // Numeric literals
        addStyles(theme.get("number"), 
        TokenType.INTEGER_LITERAL, TokenType.FLOAT_LITERAL, 
        TokenType.DOUBLE_LITERAL);
        
        // Boolean literals
        addStyles(theme.get("boolean"), 
        TokenType.TRUE, TokenType.FALSE);
        
        // Null literal
        addStyles(theme.get("null"), 
        TokenType.NULL);
        
        // Char literals
        addStyles(theme.get("char"), 
        TokenType.CHAR_LITERAL);
        
        // Operators
        addStyles(theme.get("operator"), 
        TokenType.PLUS, TokenType.MINUS, TokenType.STAR, 
        TokenType.SLASH, TokenType.MODULO, TokenType.EQUAL, 
        TokenType.BANG, TokenType.LESS, TokenType.GREATER,
        TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL, 
        TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL,
        TokenType.OR, TokenType.AND, TokenType.RIGHT_ARROW, 
        TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL,
        TokenType.STAR_EQUAL, TokenType.SLASH_EQUAL, 
        TokenType.MODULO_EQUAL, TokenType.INCREMENT, 
        TokenType.DECREMENT);
        
        addStyles(theme.get("comment"),
        TokenType.INLINE_COMMENT, TokenType.BLOCK_COMMENT);
    }
    
    private void addStyles(Color color, TokenType... tokenTypes) {
        if (color != null) {
            AttributeSet style = styleContext.addAttribute(styleContext.getEmptySet(),
            StyleConstants.Foreground, color);
            for (TokenType tokenType : tokenTypes) {
                tokenStyles.put(tokenType, style);
            }
        }
    }
    
    private void processChangedText() {
    if (isProcessing || tokens == null) return;
    
    SwingUtilities.invokeLater(() -> {
        try {
            isProcessing = true;
            
            // Get the content
            String content = getText(0, getLength());
            
            // Reset all styling to default
            setCharacterAttributes(0, content.length(), defaultStyle, true);
            
            int lastIndex = 0; // Track last position
            for (Token token : tokens) {
                AttributeSet style = tokenStyles.get(token.type);
                if (style != null) {
                    int startOffset = getTokenStartOffset(content, token, lastIndex);
                    if (startOffset >= 0) {
                        setCharacterAttributes(startOffset, token.lexeme.length(), style, false);
                        lastIndex = startOffset + token.lexeme.length(); // Move past last match
                    }
                }
            }
            
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        } finally {
            isProcessing = false;
        }
    });
}

    
private int getTokenStartOffset(String content, Token token, int lastIndex) {
    int index = content.indexOf(token.lexeme, lastIndex);
    return index >= 0 ? index : -1;
}

    
    public void setTheme(String themeName) {
        // Update the theme in ThemeManager
        ThemeManager.setTheme(themeName);
        
        // Reapply the styles based on the new theme
        applyTheme();
        
        // Re-process the text to update the syntax highlighting
        processChangedText();
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }
    
}
