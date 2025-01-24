package com.guava;

import static com.guava.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}
    
    private final List<Token> tokens;
    private int current = 0;
    private int loopDepth = 0;
    private int functionDepth = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = filterTokens(tokens);
    }
    
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        
        return statements;
    }
    
    //>> --------- --------- DECLARATIONS --------- --------- <<//    
    
    // <declaration> ::= <mainDecl> | <funcDecl> | <nativeDecl> | <varDecl> | <statement>
    private Stmt declaration() {
        try {
            Token modifier = getModifierIfExists();

            if (match(VOID)) {
                if (match(MAIN)) {
                    if (modifier == null) error(previous(), "Main declaration must precede expose modifier before void type");
                    else return mainDeclaration(modifier);
                } else {
                    Token name = consume(IDENTIFIER, "Expect function name.");
                    return functionDeclaration(modifier, previous(), name);
                }
            }

            if (isVarType()) {
                Token varType = previous();
                // Error: Using "main" with a non-void type
                if (check(MAIN)) {
                    error(varType, "'main' must have a return type of 'void'.");
                }

                Token name = consume(IDENTIFIER, "Expect identifier name after type.");

                if (check(LEFT_PAREN)) {
                    return functionDeclaration(modifier, varType, name);
                } else {
                    if (isNativeClassType(varType.type)) {
                        return nativeDeclaration(modifier, varType, name);
                    }
                    return varDeclaration(modifier, varType, name);
                }
            }

            // If there's a modifier but no valid declaration, throw an error
            if (modifier != null) {
                error(modifier, "Modifier '" + modifier.lexeme + "' must precede a valid declaration.");
            }            

            // if doesn't have type, it must be a statement
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    
    private Stmt mainDeclaration(Token modifier) {
        consume(LEFT_PAREN, "Expect '(' after main declaration.");
        
        Token paramVarType = null;
        Token paramName = null;
        
        // Check if a parameter exists
        if (isVarType()) {
            paramVarType = previous();
            paramName = consume(IDENTIFIER, "Expect parameter name.");
        }
        
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_CBRACE, "Expect '{' before main body.");
        
        
        try {
            functionDepth++;
            List<Stmt> body = block();
            
            return new Stmt.Main(modifier, paramVarType, paramName, body);
        } finally {
            functionDepth--;
        }        
        
    }  
    
    private Stmt functionDeclaration(Token modifier, Token returnType, Token name) {
        consume(LEFT_PAREN, "Expect '(' after function name.");
        
        List<Token> varTypes = new ArrayList<>();
        List<Token> parameters = new ArrayList<>();
        // Parse optional parameters
        if (!check(RIGHT_PAREN)) {
            do {
                if (isVarType()) {
                    varTypes.add(previous());
                    parameters.add(consume(IDENTIFIER, "Expect parameter name."));
                } else {
                    throw error(peek(), "Expect a valid parameter type.");
                }
            } while (match(COMMA));
        }
        
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_CBRACE, "Expect '{' before function body.");
        
        try {
            functionDepth++;
            List<Stmt> body = block();
            
            return new Stmt.Function(modifier, returnType, name, varTypes, parameters, body);
        } finally {
            functionDepth--;
        }        
    }
    
    private Stmt nativeDeclaration(Token modifier, Token varType, Token name) {
        Expr initializer = null;

        if (match(RIGHT_ARROW)) {
            initializer = expression();
        } else if (check(EQUAL)) {
             error(peek(), "Must use '->' for native assignment.");
        }

        consume(SEMICOLON, "Expect ';' after native declaration.", true);
        return new Stmt.Native(modifier, varType, name, initializer);        
    }
    
    private Stmt varDeclaration(Token modifier, Token varType, Token name) {
        Expr initializer = null;

        if (match(EQUAL)) {
            initializer = expression();
        } else if (check(RIGHT_ARROW)) {
            error(peek(), "Must use '=' for variable assignment.");
        }
        
        consume(SEMICOLON, "Expect ';' after variable declaration.", true);
        return new Stmt.Var(modifier, varType, name, initializer);
    }   
    
    
    
    //>> --------- --------- STATEMENTS --------- --------- <<//
    
    // <statement> ::= <exprStmt> | <forStmt> | <whileStmt> | <doWhileStmt>
    //              | <ifStmt> | <printStmt> | <returnStmt> | <breakStmt> | <block>    
    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(WHILE)) return whileStatement();
        if (match(DO)) return doWhileStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(BREAK)) return breakStatement();
        if (match(CONTINUE)) return continueStatement();
        if (match(LEFT_CBRACE)) return new Stmt.Block(block());
        
        return expressionStatement();
    }
    
    
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }
    
    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        
        // for-initializer
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (isVarType()) {
            Token varType = previous();
            Token identifier = consume(IDENTIFIER, "Expect identifier after type.");
            initializer = varDeclaration(varType, null, identifier);
        } else {
            initializer = expressionStatement();
        }
        
        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");
        
        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        
        try {
            loopDepth++;
            Stmt body = statement();
            
            
            if (increment != null) {
                body = new Stmt.Block(
                Arrays.asList(
                body,
                new Stmt.Expression(increment)));
            }
            
            if (condition == null) condition = new Expr.Literal(true);
            body = new Stmt.While(condition, body);
            
            if (initializer != null) {
                body = new Stmt.Block(Arrays.asList(initializer, body));
            }
            
            return body;
        } finally {
            loopDepth--;
        }
    }
    
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        
        try {
            loopDepth++;
            Stmt body = statement();
            
            return new Stmt.While(condition, body);
        } finally {
            loopDepth--;
        }
    }
    
    private Stmt doWhileStatement() {
        try {
            loopDepth++;
            Stmt body = statement();
            
            consume(WHILE, "Expect while after 'do' block.");
            
            consume(LEFT_PAREN, "Expect '(' after 'while'.");
            Expr condition = expression();
            consume(RIGHT_PAREN, "Expect ')' after condition.");
            consume(SEMICOLON, "Expect ';' after 'do-while' statement.");
            
            return new Stmt.DoWhile(body, condition);
        } finally {
            loopDepth--;
        }   
    }
    
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition."); // [parens]
        
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        
        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    
    private Stmt printStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'print'.");
        
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }
        
        consume(RIGHT_PAREN, "Expect ')' after arguments in print statement.");
        consume(SEMICOLON, "Expect ';' after print statement.");
        
        return new Stmt.Print(arguments);
    }    
    
    private Stmt returnStatement() {
        if (functionDepth == 0) {
            error(previous(), "Must be inside a function to use 'return'.");
        }
        
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }        
        
        consume(SEMICOLON, "Expect ';' after return value.", true);
        return new Stmt.Return(keyword, value);
    }
    
    private Stmt breakStatement() {
        if (loopDepth == 0) {
            error(previous(), "Must be inside a loop to use 'break'.");
        }
        consume(SEMICOLON, "Expect ';' after 'break'.", true);
        return new Stmt.Break();
    }
    
    private Stmt continueStatement() {
        if (loopDepth == 0) {
            error(previous(), "Must be inside a loop to use 'continue'.");
        }
        consume(SEMICOLON, "Expect ';' after 'continue'.");
        return new Stmt.Continue();
    }    
    
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        
        while (!check(RIGHT_CBRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        
        consume(RIGHT_CBRACE, "Expect '}' after block.");
        return statements;
    }
    
    //>> --------- --------- EXPRESSIONS --------- --------- <<//   
    
    // <expression> ::= <assignment>
    private Expr expression() {
        return assignment();
    }
    
    // <assignment> ::= ( <call> "." )? <IDENTIFIER> <assignment_op> <assignment> | <logic_or>
    // <assignment_op> ::= "=" | "+=" | "-=" | "*=" | "/="
    private Expr assignment() {
        Expr expr = or();
        
        if (match(RIGHT_ARROW, EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, MODULO_EQUAL)) {
            Token assignOp = previous();
            Expr value = assignment();
            
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, assignOp, value);
            } if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }
            
            error(assignOp, "Invalid assignment target.");
        }
        
        return expr;
    }
    
    // <logic_or> ::= <logic_and> ( "||" <logic_and> )*
    private Expr or() {
        Expr expr = and();
        
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        
        return expr;
    }
    
    // <logic_and> ::= <equality> ( "&&" <equality> )*
    private Expr and() {
        Expr expr = equality();
        
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        
        return expr;
    }
    
    // <equality> ::= <comparison> (( "!=" | "==" ) <comparison> )*
    private Expr equality() {
        Expr expr = comparison();
        
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // <comparison> ::= <term> ( ( ">" | ">=" | "<" | "<=" ) <term> )*
    private Expr comparison() {
        Expr expr = term();
        
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    
    // <term> ::= <factor> ( ("-" | "+" ) <factor> )*
    private Expr term() {
        Expr expr = factor();
        
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    
    // <factor> ::= <unary> ( ( "/" | "*" | "%" ) <unary> )*
    private Expr factor() {
        Expr expr = unary();
        
        while (match(SLASH, STAR, MODULO)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // <unary> ::= ( "!" | "-" | "++" | "--" ) <unary> | <postfix>
    private Expr unary() {
        if (match(BANG, MINUS, INCREMENT, DECREMENT)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        
        return postfix();
    }
    
    // <postfix> ::= <call> ( "++" | "--" )?    
    private Expr postfix() {
        Expr left = call();
        
        if(match(INCREMENT, DECREMENT)) {
            Token operator = previous();
            return new Expr.Postfix(left, operator);
        }
        
        return left;
    }
    
    // helper to check the arguments
    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }
        
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        
        return new Expr.Call(callee, paren, arguments);
    }    
    
    // <call> ::= <primary> ( "(" <arguments>? ")" | "." <IDENTIFIER> )*
    private Expr call() {
        Expr expr = primary();
        
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }
        
        return expr;
    }
    
    // <primary> ::= "true" | "false" | "null" | <NUMBER> | <STRING> 
    // 			| <IDENTIFIER> | (" <expression> ")" | "scan" "()"
    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);
        
        if (match(INTEGER_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL, CHAR_LITERAL, STRING_LITERAL)) {
            return new Expr.Literal(previous().lexeme);
        }
        
        if (match(IDENTIFIER)) return parseIdentifier();
        // if (match(IDENTIFIER)) return new Expr.Variable(previous());
        
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        
        if (match(READ)) {
            if (check(LEFT_PAREN)) {
                consume(LEFT_PAREN, "Expect '(' after 'read'.");
                consume(RIGHT_PAREN, "Expect '(' after 'read' expression.");
                return new Expr.Read();
            }
            return new Expr.Variable(previous());
        }
        
        if (match(COMP)) {
            consume(DOT, "Expect '.' after 'Comp'.", true);
            
            if (!isComponentType()) {
                throw error(peek(), "Expected component type.");
            }
            
            Token compType = previous();
            
            consume(LEFT_PAREN, "Expect '(' after component type.");
            
            List<Expr> arguments = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                do {
                    arguments.add(expression());
                } while (match(COMMA));
            }
            
            
            consume(RIGHT_PAREN, "Expect ')' after arguments.");
            
            List<Stmt> componentBody = new ArrayList<>();
            if (match(LEFT_CBRACE)) {
                while (!check(RIGHT_CBRACE) && !isAtEnd()) {
                    componentBody.add(componentBody());
                }
                consume(RIGHT_CBRACE, "Expect '}' after component body");
            }

            return new Expr.Comp(null, compType, arguments, null, componentBody);
        }
        
        if (match(STYLE)) {
            consume(LEFT_PAREN, "Expect '(' after 'Style'.");
            List<Token> styleArgs = styleArguments();
            consume(RIGHT_PAREN, "Expect ')' after style arguments");
            return new Expr.Style(styleArgs);
        }
        
        if (match(EVENT)) {
            consume(LEFT_PAREN, "Expect '(' after 'Event'.");
            List<EventAction> actions = eventArguments();
            consume(RIGHT_PAREN, "Expect ')' after event arguments");
            return new Expr.Event(actions);
        }
        
        throw error(peek(), "Expect expression.");
    }
    
    private Stmt componentBody() {
        if (isComponentMethod()) {
            Stmt compCall = null;
            Token compMethod = previous();
            if (compMethod.type == ADD) compCall = parseAddMethod();
            consume(SEMICOLON, "Expect ';' after " + compMethod.lexeme + " call."); 
            return compCall;
        }

        return declaration();
    }
    
    private Stmt parseAddMethod() {
        consume(LEFT_PAREN, "Expect '(' after add method.", true);
        Expr var = primary();
        // Token name = consume(IDENTIFIER, "Expect component variable to add");
        consume(RIGHT_PAREN, "Expect ')' after add method.", true);
        return new Stmt.Add(var);
    }
    
    
    private Expr parseIdentifier() {
        Token identifier = previous();

        if (check(DOT)) {
            List<Stmt> compMethods = new ArrayList<>();
            TokenType nextToken = peekNextType();
            if (nextToken == ADD) {
                while (match(DOT)) {
                    nextToken = peek().type;
                    if (nextToken == ADD) {
                        advance();
                        compMethods.add(parseAddMethod());
                    } else {
                        break;
                    }
                }
                return new Expr.Comp(identifier, null, null, compMethods, null);
            }
            
        }

        return new Expr.Variable(identifier);
    }
    
    
    //>> --------- --------- TOKEN HELPER METHODS --------- --------- <<//
    private List<Token> styleArguments() {
        List<Token> args = new ArrayList<>();
        consume(LEFT_CBRACE, "Expect '{' before style arguments.");
        
        if (!check(RIGHT_CBRACE)) {
            do {
                Token argumentString = consume(STRING_LITERAL, "Expect a string for style argument.");  
                args.add(argumentString);
            } while (match(COMMA));
        }
        
        consume(RIGHT_CBRACE, "Expect '}' after style arguments.");
        return args;
    }
    
    private List<EventAction> eventArguments() {
        List<EventAction> actions = new ArrayList<>();
        consume(LEFT_CBRACE, "Expect '{' before event arguments.");
        
        if (!check(RIGHT_CBRACE)) {
            do {
                Token eventName = consume(STRING_LITERAL, "Expect an event name as a string.", true);
                consume(COLON, "Expect ':' after event name.", true);
                consume(LEFT_CBRACE, "Expect '{' after event block.", true);
                Stmt block = new Stmt.Block(block());
                actions.add(new EventAction(eventName, block));
            } while (match(COMMA));
        }
        
        consume(RIGHT_CBRACE, "Expect '}' after event arguments.");
        return actions;
    }

    
    
    private Token getModifierIfExists() {
        return isModifier() ? previous() : null;
    }    
    
    private boolean isModifier() {
        return match(EXPOSE);
    }
    
    private boolean isReturnType() {
        return isVarType() || match(VOID);
    }
    
    private boolean isVarType() {
        return isDataType() || isNativeClassType();
    }
    
    private boolean isDataType() {
        return match(CHAR, STRING, INT, FLOAT, DOUBLE, BOOL);
    }
    
    private boolean isNativeClassType() {
        return match(COMP, STYLE, EVENT);
    }

    private boolean isNativeClassType(TokenType type) {
        return type == COMP || type == STYLE || type == EVENT;
    } 
    
    private boolean isComponentType() {
        return match(FRAME, PANEL, LABEL, BUTTON, FIELD, DIALOG);
    }

    private boolean isComponentMethod() {
        return match(ADD);
    }    

    //>> --------- --------- PARSER HELPER METHPODS --------- --------- <<//    
    
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        
        return false;
    }
    
    private Token consume(TokenType type, String message, boolean isPrevious) {
        if (check(type)) return advance();
        if (isPrevious) {
            throw error(previous(), message);
        } else {
            throw error(peek(), message);
        }
        
    }    
    
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        
        throw error(peek(), message);
    }
    
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private TokenType peekNextType() {
        if (current + 1 < tokens.size()) {
            return tokens.get(current + 1).type;
        }
        return null;
    }
    
    
    private boolean isAtEnd() {
        return peek().type == EOF;
    }    
    
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    private ParseError error(Token token, String message) {
        Guava.error(token, message);
        return new ParseError();
    }
    
    private void synchronize() {
        advance();
        
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case INT: case DOUBLE: case CHAR:
                case STRING: case VOID: case BOOL:
                case COMP: case STYLE: case EVENT:
                case FOR:
                case IF:
                case WHILE:
                case DO:
                case PRINT:
                case RETURN:
                case BREAK:
                case CONTINUE:
                return;
                default:
            }
            
            advance();
        }
    }
    
    private List<Token> filterTokens(List<Token> tokens) {
        List<Token> filteredTokens = new ArrayList<>();
        for (Token token : tokens) {
            if (token.type != BLOCK_COMMENT && token.type != INLINE_COMMENT) {
                filteredTokens.add(token);
            }
        }
        return filteredTokens;
    }    
}    
