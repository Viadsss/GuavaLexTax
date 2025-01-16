## Things to Note
- use the check method in parser for more error reporting

- add the Comp, Style, Event, and when we do new Style()

- [Adding break and continue statements](https://github.com/munificent/craftinginterpreters/blob/master/note/answers/chapter09_control.md)

private int functionDepth = 0;

private Stmt.Function function(String kind) {
functionDepth++; // Enter a function

    Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
    //... (rest of your function parsing code)

    // Ensure function body is properly parsed
    List<Stmt> body = block();

    functionDepth--;  // Exit the function
    return new Stmt.Function(name, parameters, dataTypes, returnType, body);

}

private Stmt returnStatement() {
if (functionDepth == 0) {
error(previous(), "Cannot return outside of a function.");
}
Token keyword = previous();
Expr value = null;
if (!check(SEMICOLON)) {
value = expression();
}
consume(SEMICOLON, "Expect ';' after return value.");
return new Stmt.Return(keyword, value);
}

private boolean hasMain = false;

// in lexer
private boolean isValidIdentifier(String text) {
    // A set of keywords that are allowed as valid identifiers
    Set<String> contextSensitiveKeywords = Set.of("number", "string", "bool", "style", "event", "Comp");

    // If the text is a keyword but context-sensitive, it can also be an identifier
    return contextSensitiveKeywords.contains(text) || !keywords.containsKey(text);
}

private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);

    // Look up the keyword
    TokenType type = keywords.get(text);

    if (type == null || isValidIdentifier(text)) {
        // If not a keyword, or if it can be a valid identifier, treat it as an identifier
        type = IDENTIFIER;
    }

    addToken(type);
}


class Animal {
    // Static method that returns an instance of Animal
    public static Animal dog() {
        return new Dog();
    }
    
    public void speak() {
        System.out.println("Animal speaking");
    }
}

class Dog extends Animal {
    @Override
    public void speak() {
        System.out.println("Woof!");
    }
}

public class Main {
    public static void main(String[] args) {
        // Call the static method that returns an instance of Dog
        Animal myDog = Animal.dog();  // This returns a Dog instance
        myDog.speak();
    }
}


Style styler.create -> Style({"style1, "style2"});
Event eventHandler.create -> Event("onClick": {
    <statements>
});
Comp lbl.create -> Comp.label("abc");
Comp x.create -> Comp.panel(<args>) {
    
    add(lbl);
    // or
    add(create -> Comp.label("abc"));

    add(styler);
    // or
    add(create -> Style({"style1, "style2"}));

    add(eventHandler);
    // or
    add(create -> Event({
        "onClick": {
            <statements>
            }
        ...
    }));
}


// <declaration> ::= <mainDecl> | <funcDecl> | <varDecl> | <statement>
    private Stmt declaration() {
        try {
            // Handle the case where there's a return type
            if (isReturnType()) {
                Token varType = previous();
                
                if (varType.type == VOID) {
                    if (match(MAIN)) {
                        return mainDeclaration();
                    } else {
                        Token modifier = getModifierIfExists();
                        return functionDeclaration(varType, modifier, null);
                    }
                }

                
                // Handle variable type declarations
                Token modifier = getModifierIfExists();
                Token identifier = consume(IDENTIFIER, "Expect identifier after type.");
                
                if (check(LEFT_PAREN)) return functionDeclaration(varType, modifier, identifier);
                else                   return varDeclaration(varType, modifier, identifier);
            }
            
            // If not a return type, it must be a statement
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    
    private Stmt mainDeclaration() {
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
        
        List<Stmt> body = block();
        return new Stmt.Main(paramVarType, paramName, body);
    }
    
    private Stmt functionDeclaration(Token returnType, Token modifier, Token name) {
        Token functionName;
        if (name == null) {
            functionName = consume(IDENTIFIER, "Expect function name.");
        } else {
            functionName = name;
        }
        
        consume(LEFT_PAREN, "Expect '(' after function name.");
        
        List<Token> varTypes = new ArrayList<>();
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                // if (parameters.size() >= 255) error(peek(), "Can't have more than 255 parameters.");
                Token paramVarType;
                if (isVarType()) paramVarType = previous();
                else throw error(peek(), "Expect a valid parameter type after '('.");
                
                Token paramName = consume(IDENTIFIER, "Expect parameter name.");
                
                varTypes.add(paramVarType);
                parameters.add(paramName);
            } while (match(COMMA));        
        }
        
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_CBRACE, "Expect '{' before function body.");
        List<Stmt> body = block();
        
        return new Stmt.Function(returnType, modifier, functionName, varTypes, parameters, body);
    }
    
    private Stmt varDeclaration(Token varType, Token modifier, Token identifier) {
        if (!check(EQUAL) && !check(SEMICOLON)) {
            Token invalid = peek();
            throw error(invalid, "Unexpected token '" + invalid.lexeme + "' in variable declaration");
        }
        
        
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        
        consume(SEMICOLON, "Expect ';' after variable declaration.", true);
        return new Stmt.Var(varType, modifier, identifier, initializer);
    }

private Expr unary() {
    // Check for the "!" operator, which allows recursion
    if (match(BANG)) {
        Token operator = previous();
        Expr right = unary(); // Recursively parse the next `unary`
        return new Expr.Unary(operator, right);
    }

    // Check for "-" or increment/decrement operators
    if (match(MINUS, INCREMENT, DECREMENT)) {
        Token operator = previous();

        // Disallow consecutive "-" or increment/decrement operators
        if (check(MINUS, INCREMENT, DECREMENT)) {
            error(peek(), "Cannot have consecutive unary operators like '--', '++', or '-'.");
        }

        Expr right = unary(); // Parse the next expression
        return new Expr.Unary(operator, right);
    }

    return postfix(); // Fallback to the postfix rule
}


private Expr postfix() {
    Expr left = call();

    if (match(INCREMENT, DECREMENT)) {
        Token operator = previous();

        // Disallow consecutive postfix operators
        if (check(INCREMENT, DECREMENT)) {
            error(peek(), "Cannot have consecutive postfix operators like 'x++--'.");
        }

        return new Expr.Postfix(left, operator);
    }

    return left;
}
