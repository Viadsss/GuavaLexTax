package com.guava;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class AstGenerator {
    public static void main(String[] args) throws IOException {
        
        String outputDir = "src/main/java/com/guava";
        defineAst(outputDir, "Expr", Arrays.asList(
        "Assign : Token name, Token operator, Expr value",
        "Binary : Expr left, Token operator, Expr right",
        "Call     : Expr callee, Token paren, List<Expr> arguments",
        "Get      : Expr object, Token name",
        "Set      : Expr object, Token name, Expr value",
        "Grouping : Expr expression",
        "Literal : Object value",
        "Logical : Expr left, Token operator, Expr right",
        "Unary : Token operator, Expr right",
        "Postfix : Expr left, Token operator",
        "Variable: Token name",
        "Read : ",
        
        // Component expressions
        "Component: Token identifier, Token type, List<Expr> arguments, List<Stmt> methods, List<Stmt> body",
        "StyleLiteral : List<Expr.StyleProperty> properties",
        "StyleProperty : Token name, List<Expr> values",
        "EventLiteral : List<Expr.EventProperty> properties",
        "EventProperty : Token name, Stmt action",
        "Unit : Token number, Token unit"
        ));        
        
        defineAst(outputDir, "Stmt", Arrays.asList(
        "Block : List<Stmt> statements",
        "Expression : Expr expression",
        "Function   : Token name, List<Token> params, List<Stmt> body",            
        "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
        "Return       : Token keyword, Expr value",
        "Var          : Token name, Expr initializer, List<Modifier> modifiers",
        "While        : Expr condition, Stmt body",
        "DoWhile    : Stmt body, Expr condition",
        "Print : List<Expr> arguments" ,
        
        // Component method calls as separate statements
        "Layout : Token type, List<Expr> arguments",
        "Style :  Expr styleLiteral",
        "Event :  Expr eventLiteral",
        // "Bind :  List<Expr> arguments",
        "Add :  Expr name"             
        ));
    }
    
    private static void defineAst( String outputDir, String baseName, List<String> types) throws IOException {
        String path  = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        
        writer.println("package com.guava;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        
        defineVisitor(writer, baseName, types);
        
        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim(); // [robust]
            defineType(writer, baseName, className, fields);
        }        
        
        writer.println();
        writer.println("  abstract <T> T accept(Visitor<T> visitor);");
        
        writer.println("}");
        writer.close();
    }
    
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(" interface Visitor<T> {");
        
        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    T visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        
        writer.println("  }");
    }
    
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("  static class " + className + " extends " + baseName + " {");
        
        // Handle cases where there are no fields (empty field list)
        if (fieldList.trim().isEmpty()) {
            // No fields to process, just print the constructor with no parameters
            writer.println("    " + className + "() { }");
        } else {
            // Fields exist, process normally
            if (fieldList.length() > 64) {
                fieldList = fieldList.replace(", ", ",\n          ");
            }
            
            // Constructor.
            writer.println("    " + className + "(" + fieldList + ") {");
            fieldList = fieldList.replace(",\n          ", ", ");
            
            // Store parameters in fields.
            String[] fields = fieldList.split(", ");
            for (String field : fields) {
                String name = field.split(" ")[1];
                writer.println("      this." + name + " = " + name + ";");
            }
            writer.println("    }");
        }
        
        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    <T> T accept(Visitor<T> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");
        
        // Fields.
        writer.println();
        if (!fieldList.trim().isEmpty()) {
            String[] fields = fieldList.split(", ");
            for (String field : fields) {
                writer.println("    final " + field + ";");
            }
        }
        
        writer.println("  }");
    }
    
}
