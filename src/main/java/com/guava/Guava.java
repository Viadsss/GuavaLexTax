package com.guava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import com.utils.Color;


public class Guava {
    public static List<Error> errorList = new ArrayList<>();
    public static boolean hadError = false;
    public static boolean isEditorMode = false;
    
    public static void main(String[] args) throws IOException {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("How would you like to use the Guava language?");
            System.out.println(Color.colorize(Color.COLOR_GREEN, "\t[1]") + " - Interactive REPL (enter commands directly)");
            System.out.println(Color.colorize(Color.COLOR_CYAN, "\t[2]") + " - Load and execute a file (provide a file path)");
            System.out.println(Color.colorize(Color.COLOR_BLUE, "\t[3]") + " - Open the Guava code editor");
            System.out.print("Your Choice: " + Color.COLOR_YELLOW.code);
            int choice = input.nextInt();
            System.out.println(Color.COLOR_RESET.code);

            switch (choice) {
                case 1: runPrompt();                            break;
                case 2: runFile();                              break;
                case 3: GuavaEditor.run(); isEditorMode = true; break;
                default:
                    System.out.println(Color.colorize(Color.COLOR_RED, "Invalid choice."));
                    break;
            }

        } catch (InputMismatchException e) {
            System.out.println(Color.colorize(Color.COLOR_RED, "Invalid choice."));
        }
    }

    private static void runFile() throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter Path: ");
        String path = input.nextLine();
        input.close();

        if (!path.endsWith(".gv")) {
            System.err.println(Color.colorize(Color.COLOR_RED, "Error: File must have a .gv extension."));
            System.exit(1);
        }

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        
        for (;;) { 
            System.out.print(Color.colorize(Color.COLOR_PURPLE, ">> "));
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        System.out.println("======[ LEXER ]======");
        for (Token token : tokens) {
            System.out.println(token);
        }
        System.out.println("=====================\n");
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.        
        if (hadError) return;

        System.out.println("======[ PARSER3 ]======");
        AstPrinter printer = new AstPrinter();
        TreeNode program = printer.program(statements);
        program.printTree(program, "", true);
        System.out.println("======================\n");        

    }

    public static class Error {
        private int line;
        private String message;
        private String where;
        private ErrorType type;
        
        public Error(int line, String where, String message, ErrorType type) {
            this.line = line;
            this.where = where;
            this.message = message;
            this.type = type;
        }

        public int getLine() {
            return line;
        }

        public String getMessage() {
            return message;
        }

        public ErrorType getType() {
            return type;
        }

        @Override
        public String toString() {
            return "[line " + line + "] Error" + where + ": " + message + " (" + type.toString() + ")"; 
        }
    }

    public enum ErrorType {
        LEXICAL_ERROR,
        SYNTAX_ERROR,
    }    


    // Lexical Errors (tokenization)
    public static void error(int line, String message) {
        report(line, "", message, ErrorType.LEXICAL_ERROR);
    }

    // Lexical Errors (grammar rules)
    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at the end", message, ErrorType.SYNTAX_ERROR);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message, ErrorType.SYNTAX_ERROR);
        }
    }

    private static void report(int line, String where, String message, ErrorType type) {
        Error error = new Error(line, where, message, type);

        if (!isEditorMode) {
            System.out.println(error);
        }
        errorList.add(error);
        hadError = true;
    }
    
    public static List<Error> getErrorList() {
        return errorList;
    }

    public static void cleanErrorList() {
        errorList.clear();
    }   
}
