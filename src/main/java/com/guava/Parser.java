package com.guava;

import static com.guava.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}
    
    private final List<Token> tokens;
    private int current = 0;
    
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        
        return statements;
    }
    
    private Stmt declaration() {
        try {
            if (match(FUNC)) return function("function");
            if (match(VAR)) return varDeclaration();
            
            return statement();
        } catch (ParseError error) {
            synchronize();
            System.out.println("SYNCRHONIZE");
            return null;
        }
    }    
    
    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(DO)) return doWhileStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        
        return expressionStatement();
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
    
    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        
        // for-initializer
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
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
    
    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }    
    
    private Stmt varDeclaration() {
        List<Modifier> modifiers = new ArrayList<>();
        
        if (match(EXPOSE)) modifiers.add(Modifier.EXPOSE);
        
        Token name = consume(IDENTIFIER, "Expect variable name.");
        
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer, modifiers);
    }
    
    private Stmt doWhileStatement() {
        Stmt body = statement();
        
        consume(WHILE, "Expect while after 'do' block.");
        
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        
        consume(SEMICOLON, "Expect ';' after 'do-while' statement.");
        
        return new Stmt.DoWhile(body, condition);
    }
    
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();
        
        return new Stmt.While(condition, body);
    }    
    
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }    
    
    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }    
    
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }  
    
    // <expression> ::= <assignment>
    private Expr expression() {
        return assignment();
    }
    
    // <assignment> ::= <identifier_expr> <assignment_op> <assignment> | <logic_or>
    // <identifier_expr> ::= ( <call> "." )? <IDENTIFIER>
    // <assignment_op> ::= "=" | "+=" | "-=" | "*=" | "/="
    private Expr assignment() {
        Expr expr = or();
        
        if (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, MODULO_EQUAL)) {
            Token assignOp = previous();
            Expr value = assignment();
            
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, assignOp, value);
            } else if (expr instanceof Expr.Get) {
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
        
        Token paren = consume(RIGHT_PAREN,
        "Expect ')' after arguments.");
        
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
    // 			| <IDENTIFIER> | <comp> | "(" <expression> ")" | "scan" "()"
    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);
        
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().lexeme);
        
        if (match(IDENTIFIER)) return parseIdentifier();
        // if (match(IDENTIFIER)) return new Expr.Variable(previous());
        
        if (match(READ)) {
            if (check(LEFT_PAREN)) {
                consume(LEFT_PAREN, "Expect '(' after 'read'.");
                consume(RIGHT_PAREN, "Expect '(' after 'read' expression.");
                return new Expr.Read();
            }
            return new Expr.Variable(previous());
        }
        
        if (check(LEFT_BRACE)) return parseStyleLiteral(); 
        if (check(AT_LBRACE)) return parseEventLiteral();
        
        if (match(COMP)) {
            consume(DOT, "Expect '.' after 'Comp'.");
            return parseComponent();
        }
        
        
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        
        throw error(peek(), "Expect expression.");
    }
    
    private Expr parseIdentifier() {
        Token identifier = previous();
        Expr expr = new Expr.Variable(identifier);
        
        if (check(DOT)) {
            List<Stmt> compMethods = new ArrayList<>();
            TokenType nextToken = peekNextType();
            if (nextToken == LAYOUT || nextToken == STYLE || nextToken == EVENT || nextToken == ADD) {
                while (match(DOT)) {
                    nextToken = peek().type;
                    if (nextToken == LAYOUT) {
                        advance();
                        compMethods.add(parseLayoutMethod());
                    } else if (nextToken == STYLE) {
                        advance();
                        compMethods.add(parseStyleMethod());
                    } else if (nextToken == EVENT) {
                        advance();
                        compMethods.add(parseEventMethod());
                    } else if (nextToken == ADD) { 
                        advance();
                        compMethods.add(parseAddMethod());
                    } else {
                        break;
                    }
                }
                return new Expr.Component(identifier, null, null, compMethods, null);
            }
        }
        
        return expr;        
    }
    
    private Expr parseComponent() {
        if (!match(FRAME, PANEL, LABEL, BUTTON, TEXTFIELD, PASSWORDFIELD)) {
            throw error(peek(), "Expected component type.");
        }
        
        Token compType = previous();
        
        consume(LEFT_PAREN, "Expect '(' after component type");
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after arguments");
        
        List <Stmt> compMethods = new ArrayList<>();
        while (match(DOT)) {
            if (match(LAYOUT)) compMethods.add(parseLayoutMethod());
            else if (match(STYLE)) compMethods.add(parseStyleMethod());
            else if (match(EVENT)) compMethods.add(parseEventMethod());
            else if (match(ADD)) compMethods.add(parseAddMethod());
        }
        
        List<Stmt> componentBody = new ArrayList<>();
        if (match(LEFT_BRACE)) {
            while (!check(RIGHT_BRACE) && !isAtEnd()) {
                componentBody.add(componentDeclaration());
            }
            consume(RIGHT_BRACE, "Expect '}' after component body");
        }
        
        Expr component = new Expr.Component(null, compType, arguments, compMethods, componentBody);
        return component;
    }
    
    private Stmt componentDeclaration() {
        if (match(LAYOUT)) {
            Stmt layout = parseLayoutMethod();
            consume(SEMICOLON, "Expect ';' after layout call.");
            return layout;
        }
        
        if (match(STYLE)) {
            Stmt style = parseStyleMethod();
            consume(SEMICOLON, "Expect ';' after style call.");
            return style;
        }

        if (match(EVENT)) {
            Stmt event = parseEventMethod();
            consume(SEMICOLON, "Expect ';' after event call.");
            return event;
        }

        if (match(ADD)) {
            Stmt add = parseAddMethod();
            consume(SEMICOLON, "Expect ';' after add call.");
            return add;
        }
        
        return declaration();
    }
    
    private Stmt parseLayoutMethod() {
        consume(LEFT_PAREN, "Expect '(' after layout method.");
        if (!match(FLEX, GRID, ABSOLUTE)) {
            throw error(peek(), "Expected layout type [ flex | grid | absolute ].");
        }
        
        Token layoutType = previous();
        consume(LEFT_PAREN, "Expect '(' after layout type.");
        
        List<Expr> layoutTypeArgs = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                layoutTypeArgs.add(expression());
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after layout type arguments.");
        consume(RIGHT_PAREN, "Expect ')' after layout method");
        return new Stmt.Layout(layoutType, layoutTypeArgs);
    }

    private Stmt parseAddMethod() {
        consume(LEFT_PAREN, "Expect '(' after add method.");
        Expr var = primary();
        // Token name = consume(IDENTIFIER, "Expect component variable to add");
        consume(RIGHT_PAREN, "Expect ')' after add method.");
        return new Stmt.Add(var);
    }

    private Stmt parseEventMethod() {
        consume(LEFT_PAREN, "Expect '(' after event method.");
        Expr eventArgs = null;
        if (match(IDENTIFIER)) {
            Token varName = previous();
            //
            if (match(DOT)) {
                     Expr expr = primary();
                    eventArgs = new Expr.Get(expr, varName);            
                 } else {
                     //
                     eventArgs = new Expr.Variable(varName);
                 }
        } else {
            eventArgs = parseEventLiteral();
        }        

        consume(RIGHT_PAREN, "Expect ')' after event method.");
        return new Stmt.Event(eventArgs);
    }

    private Expr parseEventLiteral() {
        consume(AT_LBRACE, "Expect '@{' to start event literal");
        List<Expr.EventProperty> properties = new ArrayList<>();
        while (!check(AT_RBRACE) && !isAtEnd()) {
            Token name = consume(IDENTIFIER, "Expect event property name");
            consume(COLON, "Expect ':' after event property name.");
            Stmt action = statement();
            properties.add(new Expr.EventProperty(name, action));
        }

        consume(AT_RBRACE, "Expect '}@' to after event literal");
        return new Expr.EventLiteral(properties);
    }

    private Stmt parseStyleMethod() {
        consume(LEFT_PAREN, "Expect '(' after style method.");
        Expr styleArgs = null;
        if (match(IDENTIFIER)) {
            Token varName = previous();
            //
            if (match(DOT)) {
                     Expr expr = primary();
                    styleArgs = new Expr.Get(expr, varName);            
                 } else {
                     //
                     styleArgs = new Expr.Variable(varName);
                 }
        } else {
            styleArgs = parseStyleLiteral();
        }
        
        consume(RIGHT_PAREN, "Expect ')' after style method");
        return new Stmt.Style(styleArgs);
    }
    
    private Expr parseStyleLiteral() {
        consume(LEFT_BRACE, "Expect '{' to start style literal");
        
        List<Expr.StyleProperty> properties = new ArrayList<>();
        while (!check(RIGHT_BRACE)) { // Parse until '}' is encountered
            Token name = consume(IDENTIFIER, "Expect style property name.");
            consume(COLON, "Expect ':' after style property name.");
            List<Expr> values = stylePropertyValues();
            properties.add(new Expr.StyleProperty(name, values));
            
            // Always expect a semicolon after each property
            consume(SEMICOLON, "Expect ';' after property.");
        }
    
        consume(RIGHT_BRACE, "Expect '}' after style literal");
        return new Expr.StyleLiteral(properties);
    }

    private List<Expr> stylePropertyValues() {
        List<Expr> values = new ArrayList<>();
        
        do {
            Expr value = stylePropertyValue();
            values.add(value);
        } while (match(COMMA));
        
        return values;
    }

    private Expr stylePropertyValue() {
        if (match(NUMBER)) {
            Token number = previous();
            
            if (match(PX, EM, PERCENT)) {
                Expr leftUnit = new Expr.Unit(number, previous());
                
                
                if (match(PLUS, MINUS, STAR, SLASH)) {
                    Token operator = previous(); 
                    Expr right = stylePropertyValue(); 
                    
                    // Return a binary expression involving units
                    return new Expr.Binary(leftUnit, operator, right);
                }
                
                return leftUnit; // Return the unit expression if no operation follows
            }
            
            // Handle standalone numbers and expressions starting with numbers
            if (match(PLUS, MINUS, STAR, SLASH)) {
                Token operator = previous();
                Expr right = expression();
                return new Expr.Binary(new Expr.Literal(number.lexeme), operator, right);
            }
            
            // Return just the number if no unit or operator follows
            return new Expr.Literal(number.lexeme);
        }
        return expression();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        
        return false;
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
                case COMP:
                case FUNC:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case DO:
                case PRINT:
                case RETURN:
                return;
                default:
            }
            
            advance();
        }
    }    
}


