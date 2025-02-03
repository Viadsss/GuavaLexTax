package com.guava;

import java.util.List;


public class AstPrinter implements Expr.Visitor<TreeNode>, Stmt.Visitor<TreeNode> {
    private TreeNode programNode;
    
    TreeNode program(List<Stmt> statements) {
        programNode = new TreeNode("Program");
        
        for (Stmt statement : statements) {
            if (statement == null) continue;
            programNode.addChild(statement.accept(this));
        }
        
        return programNode;
    }    
    
    
    //>> --------- --------- STATEMENTS --------- --------- <<//
    @Override
    public TreeNode visitMainStmt(Stmt.Main stmt) {
        TreeNode mainNode = new TreeNode("MainDeclaration");
        
        if (stmt.modifier != null) {
            mainNode.addChild(new TreeNode("Modifier: " + stmt.modifier.lexeme));
        }
        
        if (stmt.varType != null) {
            String paramText = "Name: " + stmt.paramName.lexeme;
            paramText += " (" + stmt.varType.lexeme + ")";
            
            TreeNode paramNode = new TreeNode(paramText);
            mainNode.addChild(paramNode);
        }
        
        TreeNode blockNode = new TreeNode("block");
        for (Stmt bodyStmt : stmt.body) {
            if (bodyStmt != null) blockNode.addChild(bodyStmt.accept(this));
        }
        mainNode.addChild(blockNode);
        
        return mainNode;
    }
    
    @Override
    public TreeNode visitFunctionStmt(Stmt.Function stmt) {
        TreeNode functionNode = new TreeNode("FunctionDeclaration");
        
        if (stmt.modifier != null) {
            functionNode.addChild(new TreeNode("Modifier: " + stmt.modifier.lexeme));
        }
        
        functionNode.addChild(new TreeNode("Return Type: " + stmt.returnType.lexeme));
        
        functionNode.addChild(new TreeNode("Name: " + stmt.name.lexeme));
        
        if (!stmt.params.isEmpty()) {
            TreeNode parametersNode = new TreeNode("Parameters");
            for (int i = 0; i < stmt.params.size(); i++) {
                Token param = stmt.params.get(i);
                Token paramType = stmt.varTypes.get(i);
                String paramText = "Name: " + param.lexeme;
                paramText += " (" + paramType.lexeme + ")";
                
                TreeNode paramNode = new TreeNode(paramText);
                parametersNode.addChild(paramNode);
            }
            functionNode.addChild(parametersNode);
        }
        
        
        TreeNode blockNode = new TreeNode("block");
        for (Stmt bodyStmt : stmt.body) {
            if (bodyStmt != null) {
                blockNode.addChild(bodyStmt.accept(this));
            } 
        }
        functionNode.addChild(blockNode);        
        
        return functionNode;
    }
    
    @Override
    public TreeNode visitNativeStmt(Stmt.Native stmt) {
        TreeNode varNode = new TreeNode("NativeClassDeclaration");
        
        if (stmt.modifier != null) {
            varNode.addChild(new TreeNode("Modifier: " + stmt.modifier.lexeme));
        }
        
        varNode.addChild(new TreeNode("Type: " + stmt.varType.lexeme));
        
        varNode.addChild(new TreeNode("Name: " + stmt.name.lexeme));
        
        if (stmt.initializer != null) {
            varNode.addChild(new TreeNode("->"));
            varNode.addChild(stmt.initializer.accept(this));
        }
        
        return varNode;
    }    
    
    @Override
    public TreeNode visitVarStmt(Stmt.Var stmt) {
        TreeNode varNode = new TreeNode("VariableDeclaration");
        
        if (stmt.modifier != null) {
            varNode.addChild(new TreeNode("Modifier: " + stmt.modifier.lexeme));
        }
        
        varNode.addChild(new TreeNode("Type: " + stmt.varType.lexeme));
        
        varNode.addChild(new TreeNode("Name: " + stmt.name.lexeme));
        
        if (stmt.initializer != null) {
            varNode.addChild(new TreeNode("="));
            varNode.addChild(stmt.initializer.accept(this));
        }
        
        return varNode;
    }
    
    @Override
    public TreeNode visitExpressionStmt(Stmt.Expression stmt) {
        TreeNode expressionNode = new TreeNode("expression");
        expressionNode.addChild(stmt.expression.accept(this));
        return expressionNode;
    }    
    
    @Override
    public TreeNode visitWhileStmt(Stmt.While stmt) {
        TreeNode whileNode = new TreeNode("while");
        
        TreeNode conditionNode = new TreeNode("condition");
        conditionNode.addChild(stmt.condition.accept(this)); 
        whileNode.addChild(conditionNode);
        
        TreeNode bodyNode = stmt.body.accept(this); 
        whileNode.addChild(bodyNode);
        
        return whileNode;
    }
    
    @Override
    public TreeNode visitDoWhileStmt(Stmt.DoWhile stmt) {
        TreeNode doWhileNode = new TreeNode("doWhile");
        
        TreeNode bodyNode = stmt.body.accept(this); 
        doWhileNode.addChild(bodyNode);
        
        TreeNode whileNode = new TreeNode("while");
        doWhileNode.addChild(whileNode);
        
        TreeNode conditionNode = stmt.condition.accept(this);
        doWhileNode.addChild(conditionNode);
        
        return doWhileNode;
    }
    
    @Override
    public TreeNode visitIfStmt(Stmt.If stmt) {
        TreeNode ifNode = new TreeNode("if");
        
        TreeNode conditionNode = stmt.condition.accept(this);
        ifNode.addChild(conditionNode);
        
        TreeNode thenBranchNode = stmt.thenBranch.accept(this);
        ifNode.addChild(thenBranchNode);
        
        if (stmt.elseBranch != null) {
            TreeNode elseNode = new TreeNode("else");
            TreeNode elseBranchNode = stmt.elseBranch.accept(this);
            elseNode.addChild(elseBranchNode);
            ifNode.addChild(elseNode);
        }
        
        return ifNode;
    }    
    
    
    @Override
    public TreeNode visitPrintStmt(Stmt.Print stmt) {
        TreeNode printNode = new TreeNode("Print");
        
        if (!stmt.arguments.isEmpty()) {
            TreeNode argumentsNode = new TreeNode("arguments");
            for (Expr arg : stmt.arguments) {
                TreeNode argNode = arg.accept(this);
                argumentsNode.addChild(argNode);
            }
            printNode.addChild(argumentsNode);
        }
        
        return printNode;        
    }
    
    @Override
    public TreeNode visitReturnStmt(Stmt.Return stmt) {
        TreeNode returnNode = new TreeNode("return");
        
        if (stmt.value != null) {
            TreeNode valueNode = stmt.value.accept(this); 
            returnNode.addChild(valueNode);
        }
        
        return returnNode;
    }
    
    @Override
    public TreeNode visitBreakStmt(Stmt.Break expr) {
        TreeNode breakNode = new TreeNode("break");
        return breakNode;
    }
    
    @Override
    public TreeNode visitContinueStmt(Stmt.Continue expr) {
        TreeNode breakNode = new TreeNode("continue");
        return breakNode;
    }     
    
    @Override
    public TreeNode visitBlockStmt(Stmt.Block stmt) {
        TreeNode blockNode = new TreeNode("block");
        
        for (Stmt statement : stmt.statements) {
            if (statement != null) {
                blockNode.addChild(statement.accept(this));  
            }
        }
        
        return blockNode;
    }
    
    @Override
    public TreeNode visitLayoutStmt(Stmt.Layout stmt) {
        TreeNode layoutNode = new TreeNode("Layout");
        
        layoutNode.addChild(new TreeNode("Type: " + stmt.type.lexeme));
        
        if (!stmt.arguments.isEmpty()) {
            TreeNode argumentsNode = new TreeNode("Arguments");
            for (Expr argument : stmt.arguments) {
                argumentsNode.addChild(argument.accept(this));
            }
            layoutNode.addChild(argumentsNode);
        }
        
        return layoutNode;
    }
    
    @Override
    public TreeNode visitAddStmt(Stmt.Add stmt) {
        TreeNode addNode = new TreeNode("Add");
        
        TreeNode nameNode = stmt.name.accept(this);
        addNode.addChild(nameNode);
        
        return addNode;
    }    
    
    //>> --------- --------- EXPRESSIONS --------- --------- <<//
    @Override
    public TreeNode visitAssignExpr(Expr.Assign expr) {
        TreeNode assignNode = new TreeNode("Assign");
        
        // Add the identifier node
        TreeNode identifierNode = new TreeNode("Identifier: " + expr.name.lexeme);
        assignNode.addChild(identifierNode);
        
        // Add the equals node for the assignment operator
        TreeNode assignOpNode = new TreeNode(expr.operator.lexeme);
        assignNode.addChild(assignOpNode);
        
        // Recursively add the value being assigned
        TreeNode valueNode = expr.value.accept(this);
        assignNode.addChild(valueNode);
        
        return assignNode;
    }        
    
    @Override
    public TreeNode visitBinaryExpr(Expr.Binary expr) {
        TreeNode operatorNode = new TreeNode(expr.operator.lexeme);
        
        // Add the left and right operands recursively
        TreeNode leftNode = expr.left.accept(this);
        operatorNode.addChild(leftNode);
        
        TreeNode rightNode = expr.right.accept(this);
        operatorNode.addChild(rightNode);
        
        return operatorNode;
    }        
    
    @Override
    public TreeNode visitCallExpr(Expr.Call expr) {
        // Create the root node for the call expression
        TreeNode callNode = new TreeNode("Call");
        
        // Add the callee (the function being called)
        TreeNode calleeNode = expr.callee.accept(this);
        callNode.addChild(calleeNode);
        
        // If there are arguments, add them to the tree
        if (!expr.arguments.isEmpty()) {
            TreeNode argumentsNode = new TreeNode("Arguments");
            for (Expr arg : expr.arguments) {
                TreeNode argNode = arg.accept(this);
                argumentsNode.addChild(argNode);
            }
            callNode.addChild(argumentsNode);
        }
        
        return callNode;
    }        
    
    @Override
    public TreeNode visitGetExpr(Expr.Get expr) {
        TreeNode getNode = new TreeNode("Get");
        
        // Create a node for the object (the "left-hand side" of the get)
        TreeNode objectNode = expr.object.accept(this);
        getNode.addChild(objectNode);
        
        // Create a node for the property (the "right-hand side" of the get)
        TreeNode propertyNode = new TreeNode("Property: " + expr.name.lexeme);
        getNode.addChild(propertyNode);
        
        return getNode;
    }        
    
    @Override
    public TreeNode visitSetExpr(Expr.Set expr) {
        TreeNode setNode = new TreeNode("Set");
        
        TreeNode objectNode = new TreeNode("Object");
        objectNode.addChild(expr.object.accept(this)); 
        setNode.addChild(objectNode);
        
        TreeNode propertyNode = new TreeNode("Property: " + expr.name.lexeme);
        setNode.addChild(propertyNode);
        
        TreeNode valueNode = new TreeNode("Value");
        valueNode.addChild(expr.value.accept(this));
        setNode.addChild(valueNode);
        
        return setNode;
    }        
    
    @Override
    public TreeNode visitGroupingExpr(Expr.Grouping expr) {
        TreeNode groupingNode = new TreeNode("Group");
        TreeNode innerExpressionNode = expr.expression.accept(this);
        
        groupingNode.addChild(innerExpressionNode);
        
        return groupingNode;
    }        
    
    @Override
    public TreeNode visitLiteralExpr(Expr.Literal expr) {
        String type = expr.type == null ? "" : expr.type.toLowerCase().replace("_literal", "");
        TreeNode literalNode = new TreeNode("Literal: " + (expr.value == null ? "null" : expr.value.toString()) + (type.isEmpty() ? "" : " (" + type + ")"));
        return literalNode;
    }        
    
    @Override
    public TreeNode visitLogicalExpr(Expr.Logical expr) {
        TreeNode logicalNode = new TreeNode(expr.operator.lexeme);
        
        TreeNode leftNode = expr.left.accept(this);  
        logicalNode.addChild(leftNode);
        
        TreeNode rightNode = expr.right.accept(this);  
        logicalNode.addChild(rightNode);
        
        return logicalNode;
    }        
    
    @Override
    public TreeNode visitUnaryExpr(Expr.Unary expr) {
        TreeNode unaryNode = new TreeNode("unary");
        
        TreeNode operatorNode = new TreeNode("operator: " + expr.operator.lexeme);
        unaryNode.addChild(operatorNode);        
        
        TreeNode rightNode = expr.right.accept(this);
        unaryNode.addChild(rightNode);
        return unaryNode;
    }        
    
    public TreeNode visitPostfixExpr(Expr.Postfix expr) {
        TreeNode postfixNode = new TreeNode("postfix");
        
        TreeNode leftNode = new TreeNode("left");
        leftNode.addChild(expr.left.accept(this));  
        postfixNode.addChild(leftNode);
        
        TreeNode operatorNode = new TreeNode("operator: " + expr.operator.lexeme);
        postfixNode.addChild(operatorNode);
        
        return postfixNode;
    }        
    
    @Override
    public TreeNode visitVariableExpr(Expr.Variable expr) {
        TreeNode variableNode = new TreeNode("Identifier: " + expr.name.lexeme);
        return variableNode;
    }        
    
    
    @Override
    public TreeNode visitReadExpr(Expr.Read expr) {
        TreeNode readNode = new TreeNode("Read");
        return readNode;
    }
    
    @Override
    public TreeNode visitCompExpr(Expr.Comp expr) {
        TreeNode compNode = new TreeNode("Comp: " + (expr.name != null ? expr.name.lexeme : expr.type.lexeme));
        
        if (expr.arguments != null && !expr.arguments.isEmpty()) {
            TreeNode argumentsNode = new TreeNode("Arguments");
            for (Expr argument : expr.arguments) {
                argumentsNode.addChild(argument.accept(this));
            }
            compNode.addChild(argumentsNode);
        }
        
        if (expr.methods != null && !expr.methods.isEmpty()) {
            TreeNode methodsNode = new TreeNode("methods");
            for (Stmt method : expr.methods) {
                TreeNode methodNode = method.accept(this);
                methodsNode.addChild(methodNode);;
            }
            compNode.addChild(methodsNode);
        }        
        
        if (expr.body != null && !expr.body.isEmpty()) {
            TreeNode bodyNode = new TreeNode("Comp body");
            for (Stmt bodyStmt : expr.body) {
                if (bodyStmt != null) bodyNode.addChild(bodyStmt.accept(this));
            }
            compNode.addChild(bodyNode);
        }
        
        return compNode;
    }    
    
    @Override
    public TreeNode visitStyleExpr(Expr.Style expr) {
        TreeNode styleNode = new TreeNode("Style");
        
        for (Token styleArgument : expr.arguments) {
            styleNode.addChild(new TreeNode("Argument: " + styleArgument.lexeme));
        }        
        
        return styleNode;
    }
    
    @Override
    public TreeNode visitEventExpr(Expr.Event expr) {
        TreeNode eventNode = new TreeNode("Event");
        
        for (EventAction action : expr.actions) {
            TreeNode actionNode = new TreeNode("Action");
            actionNode.addChild(new TreeNode("Name: " + action.name.lexeme));
            actionNode.addChild(action.block.accept(this));
            eventNode.addChild(actionNode);
        }
        
        return eventNode;
    }
    
}

