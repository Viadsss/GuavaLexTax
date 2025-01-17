package com.guava;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class GuavaEditor extends JFrame {
    private JTabbedPane bottomPanel;    
    private JTextPane lexerPane, parserPane, problemPane;
    private JScrollPane lexerScrollPane, parserScrollPane, probScrollPane;
    RSyntaxTextArea textArea;
    
    public GuavaEditor() {
        setTitle("Guava Code Editor");
        setSize(1366, 768); // 1280 x 720
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setJMenuBar(createMenuBar());
        init();
    }
    
    
    private void init() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel(), bottomPanel());     
        
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(13);
        
        this.add(splitPane);
    }
    
    private JPanel topPanel() {
        JPanel topPanel = new JPanel(new MigLayout("fill, wrap", "[grow]", "[grow,fill]"));

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.BOLD, 14));

        RTextScrollPane sp = new RTextScrollPane(textArea);
                       
        topPanel.add(sp, "grow");
        return topPanel;
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem runItem = new JMenuItem("Run Code");
        runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        runItem.addActionListener(e -> handleRun());
        
        JMenuItem uploadItem = new JMenuItem("Upload");
        uploadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        uploadItem.addActionListener(e -> handleUpload());
        
        fileMenu.add(runItem);
        fileMenu.add(uploadItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        
        JMenuItem darkModeItem = new JMenuItem("Switch to Dark Mode");
        darkModeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK));
        darkModeItem.addActionListener(e -> switchTheme(true));
        
        JMenuItem lightModeItem = new JMenuItem("Switch to Light Mode");
        lightModeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK));
        lightModeItem.addActionListener(e -> switchTheme(false));
        
        viewMenu.add(darkModeItem);
        viewMenu.add(lightModeItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        
        return menuBar;
    }
    
    
    private void switchTheme(boolean dark) {
        try {
            if (dark) {
                UIManager.setLookAndFeel(new FlatMacDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatMacLightLaf());
            }
            SwingUtilities.updateComponentTreeUI(this);
            updateThemeColors();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
    
    private JTabbedPane bottomPanel() {
        bottomPanel = new JTabbedPane();
        
        
        bottomPanel.putClientProperty(FlatClientProperties.STYLE, "" + "tabType:card");
        bottomPanel.add("Lexer Output", lexerPane());
        bottomPanel.add("Parser Output (WIP)", parserPane());
        bottomPanel.add("Problems", problemPane());
        
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
    
    private void handleRun() {
        String source = textArea.getText();
        
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();
        handleLexerOutput(tokens);
        
        
        List<String> errors = Guava.getErrorList();
        
        updateProblemsTab(errors);
        
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
                textArea.setText(content.toString()); // Set the file content to the textPane
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void updateProblemsTab(List<String> errors) {
        problemPane.setText("");
        
        StyledDocument doc = problemPane.getStyledDocument();
        
        if (errors == null || errors.isEmpty()) {
            bottomPanel.setTitleAt(2, "Problems");
            return;
        }
        
        bottomPanel.setTitleAt(2, "Problems - " + errors.size());
        
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
    
    public static void run() {
        FlatInspector.install("ctrl shift alt T");
        FlatUIDefaultsInspector.install("ctrl shift alt Y");
        
        FlatLaf.registerCustomDefaultsSource("themes");
        FlatMacLightLaf.setup();
        
        FlatJetBrainsMonoFont.install();
        UIManager.put("defaultFont", new Font(FlatJetBrainsMonoFont.FAMILY, Font.BOLD, 14));
        
        SwingUtilities.invokeLater(() -> new GuavaEditor().setVisible(true));
    }
    
    private void updateThemeColors() throws Exception {
        String currentLookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        Theme theme;
        
        if (currentLookAndFeel.contains("FlatMacDarkLaf")) {
            theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"
                ));
            theme.apply(textArea);

            textArea.setBackground(new Color(30, 30, 30)); 
            
            lexerPane.setBackground(new Color(30, 30, 30));
            lexerScrollPane.getViewport().setBackground(new Color(30, 30, 30));
            
            parserPane.setBackground(new Color(30, 30, 30));
            parserScrollPane.getViewport().setBackground(new Color(30, 30, 30));
            
            problemPane.setBackground(new Color(30, 30, 30));
            probScrollPane.getViewport().setBackground(new Color(30, 30, 30));



            
        } else if (currentLookAndFeel.contains("FlatMacLightLaf")) {
            theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/default.xml"
                ));            
            theme.apply(textArea);

            textArea.setBackground(new Color(255, 255, 255)); 
            
            lexerPane.setBackground(new Color(255, 255, 255)); 
            lexerScrollPane.getViewport().setBackground(new Color(255, 255, 255));
            
            parserPane.setBackground(new Color(255, 255, 255)); 
            parserScrollPane.getViewport().setBackground(new Color(255, 255, 255));
            
            problemPane.setBackground(new Color(255, 255, 255)); 
            probScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        }

        textArea.setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.BOLD, 14));
    }
}
