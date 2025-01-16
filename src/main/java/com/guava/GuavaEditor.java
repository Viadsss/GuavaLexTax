package com.guava;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class GuavaEditor extends JFrame {
    private JTabbedPane bottomPanel;    
    private JTextPane textPane, lexerPane, parserPane, problemPane, terminalPane;
    private JScrollPane textScrollPane, lexerScrollPane, parserScrollPane, probScrollPane, terminalScrollPane;
    private SyntaxHighlighter highlighter;
    
    public GuavaEditor() {
        setTitle("Guava Code Editor");
        setSize(1366, 768); // 1280 x 720
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        highlighter = new SyntaxHighlighter();
        init();
    }
    
    
    private void init() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel(), bottomPanel());     
        
        // splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(13);
        
        addTabKeyBehavior(textPane);
        
        this.add(splitPane);
    }
    
    private JPanel topPanel() {
        JPanel topPanel = new JPanel(new MigLayout("fill, wrap", "[grow]", "[grow,fill]"));
        textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
        };
        
        
        // Set up for code editing
        textPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        textPane.setStyledDocument(highlighter);
        
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleDocumentChange();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                handleDocumentChange();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                handleDocumentChange();
            }
        });        
        
        textScrollPane = new JScrollPane(
        textPane,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        
        textScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        // Make scrolling smoother
        textScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        textScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        LineNumberComponent lineNumber = new LineNumberComponent(textPane);
        textScrollPane.setRowHeaderView(lineNumber);                        
        topPanel.add(textScrollPane, "grow");
        return topPanel;
    }
    
    private JTabbedPane bottomPanel() {
        bottomPanel = new JTabbedPane();
        
        
        bottomPanel.putClientProperty(FlatClientProperties.STYLE, "" + "tabType:card");
        bottomPanel.add("Terminal", terminalPane());
        bottomPanel.add("Lexer Output", lexerPane());
        bottomPanel.add("Parser Output", parserPane());
        bottomPanel.add("Problems", problemPane());
        
        // Terminal Tab
        return bottomPanel;
    }
    
    private JPanel lexerPane() {
        JPanel lexer = new JPanel(new MigLayout("fill, wrap", "[grow]", "[grow,fill]"));
        lexerPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
        };
        // Set up for code editing
        lexerPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        lexerPane.setStyledDocument(new DefaultStyledDocument());
        lexerPane.setEditable(false);
        
        
        
        lexerScrollPane = new JScrollPane(
        lexerPane,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        
        lexerScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        // Make scrolling smoother
        lexerScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        lexerScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        lexerPane.putClientProperty(FlatClientProperties.STYLE, "inactiveBackground: #ffffff");
        lexer.add(lexerScrollPane, "grow");
        
        return lexer;
    }
    
    private JPanel parserPane() {
        JPanel parser = new JPanel(new MigLayout("fill, wrap", "[grow]", "[grow,fill]"));
        parserPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
        };
        // Set up for code editing
        parserPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        parserPane.setStyledDocument(new DefaultStyledDocument());
        parserPane.setEditable(false);
        
        
        
        parserScrollPane = new JScrollPane(
        parserPane,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        
        parserScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        // Make scrolling smoother
        parserScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        parserScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        parserPane.putClientProperty(FlatClientProperties.STYLE, "inactiveBackground: #ffffff");
        parser.add(parserScrollPane, "grow");
        
        return parser;
    }    
    
    private JPanel problemPane() {
        JPanel problem = new JPanel(new MigLayout("fill, wrap", "[grow]", "[grow,fill]"));
        problemPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
        };
        // Set up for code editing
        problemPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        problemPane.setStyledDocument(new DefaultStyledDocument());
        problemPane.setEditable(false);
        
        
        
        probScrollPane = new JScrollPane(
        problemPane,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        
        probScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        // Make scrolling smoother
        probScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        probScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        problemPane.putClientProperty(FlatClientProperties.STYLE, "inactiveBackground: #ffffff");
        
        
        problem.add(probScrollPane, "grow");
        
        return problem;
    }    
    
    private JPanel terminalPane() {
        JPanel terminal = new JPanel(new MigLayout("fill, wrap", "[grow]", "[grow,fill]"));
        terminalPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
        };
        // Set up for code editing
        terminalPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        terminalPane.setStyledDocument(new DefaultStyledDocument());
        terminalPane.setText("> ");
        
        
        terminalScrollPane = new JScrollPane(
        terminalPane,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        
        terminalScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        // Make scrolling smoother
        terminalScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        terminalScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        terminalPane.putClientProperty(FlatClientProperties.STYLE, "inactiveBackground: #ffffff");
        
        
        terminal.add(terminalScrollPane, "grow");
        
        terminalPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    handleCommand(terminalPane);
                }
            }
        });
        
        return terminal;
    }
    
    private void handleCommand(JTextPane terminalPane) {
        try {
            StyledDocument doc = terminalPane.getStyledDocument();
            int startOffset = findLastPromptOffset(doc);
            int endOffset = doc.getLength();
            
            // Extract the command text
            String command = doc.getText(startOffset, endOffset - startOffset).trim();
            
            // Prevent adding text mid-document
            terminalPane.setEditable(false);
            
            // Execute the command
            if (command.equalsIgnoreCase("run")) {
                doc.insertString(doc.getLength(), "\nRunning.\n", null);
                handleRun();
            } else if (command.equalsIgnoreCase("upload")) {
                doc.insertString(doc.getLength(), "\nUploading.\n", null);
                handleUpload();
            } else if (command.equalsIgnoreCase("clear")) {
                terminalPane.setText("");
            } else if (command.equalsIgnoreCase("dark")) {
                try {
                    UIManager.setLookAndFeel(new FlatMacDarkLaf());
                    SwingUtilities.updateComponentTreeUI(this);
                    updateThemeColors();
                    doc.insertString(doc.getLength(), "\nSwitched to Dark Mode\n", null);
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
            } else if (command.equalsIgnoreCase("light")) {
                try {
                    UIManager.setLookAndFeel(new FlatMacLightLaf());
                    SwingUtilities.updateComponentTreeUI(this);
                    updateThemeColors();
                    doc.insertString(doc.getLength(), "\nSwitched to Light Mode\n", null);
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
            } else if (command.equalsIgnoreCase("help")){
                doc.insertString(doc.getLength(), "\nOptions available - [run], [help], [upload], [clear], [light], [dark]\n", null);
            } else {
                doc.insertString(doc.getLength(), "\nUnknown command: " + command + ", type 'help' to list out commands\n", null);
            }
            
            // Add a new prompt and enable editing for the next input
            doc.insertString(doc.getLength(), "> ", null);
            terminalPane.setEditable(true);
            
            // Set caret position at the end
            terminalPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private int findLastPromptOffset(StyledDocument doc) {
        try {
            String text = doc.getText(0, doc.getLength());
            int promptIndex = text.lastIndexOf("> ");
            return promptIndex == -1 ? 0 : promptIndex + "> ".length();
        } catch (BadLocationException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    private void handleDocumentChange() {
        SwingUtilities.invokeLater(() -> {
            handleRun(); // Re-run the lexer
        });
    }
    
    private void handleRun() {
        String source = textPane.getText();
        // new GuavaSyntaxHighlighter(textPane);
        
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();
        handleLexerOutput(tokens);
        
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        
        List<String> errors = Guava.getErrorList();
        
        updateProblemsTab(errors);
        
        // No errors, proceed with AST output
        if (errors.isEmpty()) {
            handleParserOutput(statements);
        }
    }
    
    private List<Token> handleLexerOutput(List<Token> tokens) {
        
        StyledDocument doc = lexerPane.getStyledDocument();
        lexerPane.setText(""); // Clear the existing content
        
        // Define styles for different token components
        StyleContext styleContext = new StyleContext();
        Style typeStyle = styleContext.addStyle("TypeStyle", null);
        Style lexemeStyle = styleContext.addStyle("LexemeStyle", null);
        Style literalStyle = styleContext.addStyle("LiteralStyle", null);
        Style lineStyle = styleContext.addStyle("LineStyle", null);
        
        // Set colors for each style
        StyleConstants.setForeground(typeStyle, new Color(137, 180, 250)); // blue
        StyleConstants.setForeground(lexemeStyle, new Color(234, 118, 203)); // magenta
        StyleConstants.setForeground(literalStyle, new Color(234, 157, 52)); // dawn
        StyleConstants.setForeground(lineStyle, new Color(64, 160, 43)); // green
        
        try {
            for (Token token : tokens) {
                // Add `token.type` with blue color
                doc.insertString(doc.getLength(), "Token (type: ", null);
                doc.insertString(doc.getLength(), token.type.toString(), typeStyle);
                
                // Add `token.lexeme` with magenta color
                doc.insertString(doc.getLength(), ", lexeme: ", null);
                doc.insertString(doc.getLength(), token.lexeme, lexemeStyle);
                
                // Add `token.literal` with dawn color, if not null
                doc.insertString(doc.getLength(), ", literal: ", null);
                if (token.literal != null) {
                    doc.insertString(doc.getLength(), token.literal.toString(), literalStyle);
                } else {
                    doc.insertString(doc.getLength(), "null", literalStyle);
                }                
                
                // Add `token.line` with green color
                doc.insertString(doc.getLength(), ", line: ", null);
                doc.insertString(doc.getLength(), String.valueOf(token.line), lineStyle);
                
                // Add closing parenthesis and a newline
                doc.insertString(doc.getLength(), ")\n", null);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        return tokens;
    }
    
    private void handleParserOutput(List<Stmt> statements) {
        AstPrinter printer = new AstPrinter();
        TreeNode program = printer.program(statements);
        // program.printTree(program, "", true);
        StringBuilder builder = new StringBuilder();
        program.buildTreeString(program, "", true, builder);
        String treeString = builder.toString();
        
        parserPane.setText(treeString);
    }
    
    private void handleUpload() {
        // Create a JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a Guava (.gv) File");
        
        // Add a file filter for .gv files
        fileChooser.setFileFilter(new FileNameExtensionFilter("Guava Files (*.gv)", "gv"));
        
        // Show the dialog and get the user's selection
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File selectedFile = fileChooser.getSelectedFile();
            
            // Check if the file has the correct extension
            if (!selectedFile.getName().endsWith(".gv")) {
                JOptionPane.showMessageDialog(this, "Please select a valid .gv file.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Read the file content and set it to the textPane
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                textPane.setText(content.toString()); // Set the file content to the textPane
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void updateProblemsTab(List<String> errors) {
        problemPane.setText("");
        
        StyledDocument doc = problemPane.getStyledDocument();
        
        if (errors == null || errors.isEmpty()) {
            bottomPanel.setTitleAt(3, "Problems");
            return;
        }
        
        bottomPanel.setTitleAt(3, "Problems - " + errors.size());
        
        for (String error : errors) {
            try {
                Style style = doc.addStyle("ErrorStyle", null);
                StyleConstants.setForeground(style, Color.RED); // Set text color to red
                doc.insertString(doc.getLength(), error + "\n", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        
        Guava.cleanErrorList();
    }
    
    public class LineNumberComponent extends JComponent {
        private final JTextPane textPane;
        private final FontMetrics fontMetrics;
        private final int fontAscent;
        
        public LineNumberComponent(JTextPane textPane) {
            this.textPane = textPane;
            Font font = textPane.getFont();
            fontMetrics = getFontMetrics(font);
            fontAscent = fontMetrics.getAscent();
            
            textPane.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    repaint();
                }
                public void removeUpdate(DocumentEvent e) {
                    repaint();
                }
                public void changedUpdate(DocumentEvent e) {
                    repaint();
                }
            });
            
            textPane.addCaretListener(e -> repaint());
        }
        
        @Override
        public Dimension getPreferredSize() {
            int lineCount = getLineCount();
            int maxDigits = String.valueOf(lineCount).length();
            int width = fontMetrics.charWidth('0') * maxDigits + 20;
            return new Dimension(width, textPane.getHeight());
        }
        
        private int getLineCount() {
            Element root = textPane.getDocument().getDefaultRootElement();
            return root.getElementCount();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle clip = g.getClipBounds();
            int startOffset = textPane.viewToModel2D(new Point(0, clip.y));
            int endOffset = textPane.viewToModel2D(new Point(0, clip.y + clip.height));
            
            int componentWidth = getWidth();
            
            while (startOffset <= endOffset) {
                try {
                    Rectangle2D rect = textPane.modelToView2D(startOffset);
                    int lineNumber = getLineNumber(startOffset);
                    if (rect != null) {
                        int y = (int) (rect.getY() + rect.getHeight() / 2 + fontAscent / 2 - 2); // Center vertically
                        String lineNumberStr = String.valueOf(lineNumber);
                        int stringWidth = fontMetrics.stringWidth(lineNumberStr);
                        int x = componentWidth - stringWidth - 5; // Align to the right with padding
                        g.drawString(lineNumberStr, x, y);
                    }
                    startOffset = Utilities.getRowEnd(textPane, startOffset) + 1;
                } catch (BadLocationException e) {
                    break;
                }
            }
        }
        
        
        private int getLineNumber(int offset) {
            Element root = textPane.getDocument().getDefaultRootElement();
            return root.getElementIndex(offset) + 1;
        }
    }
    
    private void addTabKeyBehavior(JTextPane textPane) {
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    e.consume(); // Prevent the default tab behavior
                    try {
                        // Insert four spaces at the caret position
                        StyledDocument doc = textPane.getStyledDocument();
                        int caretPosition = textPane.getCaretPosition();
                        doc.insertString(caretPosition, "    ", null); // Four spaces
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
    
    
    
    public static void run() {
        FlatInspector.install("ctrl shift alt T");
        FlatUIDefaultsInspector.install("ctrl shift alt Y");
        
        FlatLaf.registerCustomDefaultsSource("themes");
        FlatMacLightLaf.setup();
        
        FlatJetBrainsMonoFont.install();
        UIManager.put("defaultFont", new Font(FlatJetBrainsMonoFont.FAMILY, Font.BOLD, 14));
        
        SwingUtilities.invokeLater(() -> new GuavaEditor().setVisible(true));
    }
    
    private void updateThemeColors() {
        String currentLookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        
        if (currentLookAndFeel.contains("FlatMacDarkLaf")) {
            textPane.setBackground(new Color(30, 30, 30)); 
            textScrollPane.getViewport().setBackground(new Color(30, 30, 30));

            lexerPane.setBackground(new Color(30, 30, 30));
            lexerScrollPane.getViewport().setBackground(new Color(30, 30, 30));

            parserPane.setBackground(new Color(30, 30, 30));
            parserScrollPane.getViewport().setBackground(new Color(30, 30, 30));
            
            problemPane.setBackground(new Color(30, 30, 30));
            probScrollPane.getViewport().setBackground(new Color(30, 30, 30));

            terminalPane.setBackground(new Color(30, 30, 30));
            terminalScrollPane.getViewport().setBackground(new Color(30, 30, 30));

            highlighter.setTheme("dark");
        } else if (currentLookAndFeel.contains("FlatMacLightLaf")) {
            textPane.setBackground(new Color(255, 255, 255)); 
            textScrollPane.getViewport().setBackground(new Color(255, 255, 255));

            lexerPane.setBackground(new Color(255, 255, 255)); 
            lexerScrollPane.getViewport().setBackground(new Color(255, 255, 255));

            parserPane.setBackground(new Color(255, 255, 255)); 
            parserScrollPane.getViewport().setBackground(new Color(255, 255, 255));
            
            problemPane.setBackground(new Color(255, 255, 255)); 
            probScrollPane.getViewport().setBackground(new Color(255, 255, 255));
            
            terminalPane.setBackground(new Color(255, 255, 255)); 
            terminalScrollPane.getViewport().setBackground(new Color(255, 255, 255));            

            highlighter.setTheme("light");
        }
    }
}
