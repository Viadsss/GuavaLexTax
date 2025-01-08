package com.guava;

import java.util.List;

class AstPrinter implements Expr.Visitor<TreeNode>, Stmt.Visitor<TreeNode> {
    private TreeNode root;
    
    TreeNode program(List<Stmt> statements) {
        root = new TreeNode("Program");
        
        for (Stmt statement : statements) {
            if (statement == null) continue;
            root.addChild(statement.accept(this));
        }
        
        return root;
    }

    @Override
    public TreeNode visitEventLiteralExpr(Expr.EventLiteral expr) {
        TreeNode objectNode = new TreeNode("Event Object");

        for (Expr.EventProperty property : expr.properties) {
            TreeNode propertyNode = property.accept(this);
            objectNode.addChild(propertyNode);
        }

        return objectNode;
    }

    @Override
    public TreeNode visitEventPropertyExpr(Expr.EventProperty expr) {
        TreeNode propertyNode = new TreeNode("Event Property");
        propertyNode.addChild(new TreeNode("Name: " + expr.name.lexeme));
        TreeNode valueNode = new TreeNode("Action: ");
        valueNode.addChild(expr.action.accept(this));
        propertyNode.addChild(valueNode);
        return propertyNode;
    }

    @Override
    public TreeNode visitStyleLiteralExpr(Expr.StyleLiteral expr) {
        TreeNode objectNode = new TreeNode("Style Object");

        for (Expr.StyleProperty property : expr.properties) {
            TreeNode propertyNode = property.accept(this);
            objectNode.addChild(propertyNode);
        }

        return objectNode;
     }

    @Override
    public TreeNode visitStylePropertyExpr(Expr.StyleProperty expr) {
        TreeNode propertyNode = new TreeNode("Style Property");
        propertyNode.addChild(new TreeNode("Name: " + expr.name.lexeme));
        TreeNode valueNode = new TreeNode("Value: ");
        for (Expr value : expr.values) {
            valueNode.addChild(value.accept(this));
        }

        propertyNode.addChild(valueNode);
        return propertyNode;
    }

    @Override
    public TreeNode visitComponentExpr(Expr.Component expr) {
        TreeNode compNode = new TreeNode("COMPONENT: " + (expr.identifier != null ? expr.identifier.lexeme : expr.type.type));

        if (expr.arguments != null && !expr.arguments.isEmpty()) {
            TreeNode argumentsNode = new TreeNode("arguments");
            for (Expr arg : expr.arguments) {
                TreeNode argNode = arg.accept(this);
                argumentsNode.addChild(argNode);
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
            TreeNode bodyNode = new TreeNode("comp block");
            for (Stmt statement: expr.body) {
                bodyNode.addChild(statement.accept(this));
            }
            compNode.addChild(bodyNode);
        }

        return compNode;
    }

    @Override
    public TreeNode visitAddStmt(Stmt.Add stmt) {
        TreeNode addnode = new TreeNode("ADD:");
        addnode.addChild(stmt.name.accept(this));
        return addnode;
    }

    @Override
    public TreeNode visitEventStmt(Stmt.Event stmt) {
        TreeNode eventNode = new TreeNode("STYLE");

        TreeNode objectNode = stmt.eventLiteral.accept(this);
        eventNode.addChild(objectNode);

        return eventNode;
    }

    @Override
    public TreeNode visitStyleStmt(Stmt.Style stmt) {
        TreeNode styleNode = new TreeNode("STYLE");

        TreeNode objectNode = stmt.styleLiteral.accept(this);
        styleNode.addChild(objectNode); 
        
        return styleNode;
    }

    @Override
    public TreeNode visitLayoutStmt(Stmt.Layout stmt) {
        TreeNode layoutNode = new TreeNode("LAYOUT: " + stmt.type.lexeme);
       if (!stmt.arguments.isEmpty()) {
            TreeNode argumentsNode = new TreeNode("arguments");
            for (Expr arg : stmt.arguments) {
                TreeNode argNode = arg.accept(this);
                argumentsNode.addChild(argNode);
            }
            layoutNode.addChild(argumentsNode);
        }        
        return layoutNode;
    }

    @Override
    public TreeNode visitReadExpr(Expr.Read expr) {
        TreeNode readNode = new TreeNode("READ");
        return readNode;
    }

    @Override
    public TreeNode visitPrintStmt(Stmt.Print stmt) {
        TreeNode printNode = new TreeNode("PRINT");

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
    public TreeNode visitBlockStmt(Stmt.Block stmt) {
        // Create a root node for the block
        TreeNode blockNode = new TreeNode("block");
        
        // Add each statement in the block as a child of the block node
        for (Stmt statement : stmt.statements) {
            if (statement != null) {
                blockNode.addChild(statement.accept(this));  // Visit each statement and add the resulting TreeNode
            }
        }
        
        return blockNode;
    }
    
    
    @Override
    public TreeNode visitExpressionStmt(Stmt.Expression stmt) {
        TreeNode expressionNode = new TreeNode("expression");
        expressionNode.addChild(stmt.expression.accept(this));
        return expressionNode;
    }
    
    @Override
    public TreeNode visitFunctionStmt(Stmt.Function stmt) {
        // Create the root node for the function
        TreeNode functionNode = new TreeNode("func");
        
        // Add the function name node
        TreeNode nameNode = new TreeNode("IDENTIFIER: " + stmt.name.lexeme);
        functionNode.addChild(nameNode);
        
        // Add the parameters node if they exist
        if (!stmt.params.isEmpty()) {
            TreeNode parametersNode = new TreeNode("PARAMETERS");
            for (Token param : stmt.params) {
                TreeNode paramNode = new TreeNode("IDENTIFIER: " + param.lexeme);
                parametersNode.addChild(paramNode);
            }
            functionNode.addChild(parametersNode);
        }
        
        // Add the block (body) of the function
        TreeNode blockNode = new TreeNode("block");
        for (Stmt bodyStmt : stmt.body) {
            blockNode.addChild(bodyStmt.accept(this));
        }
        functionNode.addChild(blockNode);
        
        return functionNode;
    }
    
    
    @Override
    public TreeNode visitIfStmt(Stmt.If stmt) {
        // Create the root node for the "if" statement
        TreeNode ifNode = new TreeNode("if");
        
        // Add the condition node (the condition part of the "if")
        TreeNode conditionNode = stmt.condition.accept(this);
        ifNode.addChild(conditionNode);
        
        // Add the "then" branch node
        TreeNode thenBranchNode = stmt.thenBranch.accept(this);
        ifNode.addChild(thenBranchNode);
        
        // Add the "else" branch node if it exists
        if (stmt.elseBranch != null) {
            TreeNode elseNode = new TreeNode("else");
            TreeNode elseBranchNode = stmt.elseBranch.accept(this);
            elseNode.addChild(elseBranchNode);
            ifNode.addChild(elseNode);
        }
        
        return ifNode;
    }
    
    
    @Override
    public TreeNode visitReturnStmt(Stmt.Return stmt) {
        // Create the root node for the return statement
        TreeNode returnNode = new TreeNode("return");
        
        // Add the value node, if present
        if (stmt.value != null) {
            TreeNode valueNode = stmt.value.accept(this); // Recursively visit the return value expression
            returnNode.addChild(valueNode);
        }
        
        return returnNode;
    }
    
    
    @Override
    public TreeNode visitVarStmt(Stmt.Var stmt) {
        TreeNode varNode = new TreeNode("varDecl");
        
        // Add modifiers
        if (stmt.modifiers != null && !stmt.modifiers.isEmpty()) {
            TreeNode modifierNode = new TreeNode("modifiers");
            for (Modifier modifier : stmt.modifiers) {
                if (modifier != null) {
                    modifierNode.addChild(new TreeNode(modifier.toString()));
                }
            }
            varNode.addChild(modifierNode);
        }
        
        varNode.addChild(new TreeNode("IDENTIFIER: " + stmt.name.lexeme));
        
        // Add initializer if present
        if (stmt.initializer != null) {
            varNode.addChild(new TreeNode("="));
            varNode.addChild(stmt.initializer.accept(this));
        }
        return varNode;
    }
    
    @Override
    public TreeNode visitWhileStmt(Stmt.While stmt) {
        // Create the root node for the while statement
        TreeNode whileNode = new TreeNode("while");
        
        // Add the condition node
        TreeNode conditionNode = new TreeNode("condition");
        conditionNode.addChild(stmt.condition.accept(this)); // Recursively visit the condition
        whileNode.addChild(conditionNode);
        
        // Add the body node
        TreeNode bodyNode = stmt.body.accept(this); // Recursively visit the body
        whileNode.addChild(bodyNode);
        
        return whileNode;
    }
    
    
    @Override
    public TreeNode visitDoWhileStmt(Stmt.DoWhile stmt) {
        // Create the root node for the do-while statement
        TreeNode doWhileNode = new TreeNode("doWhile");
        
        // Add the body of the do-while statement
        TreeNode bodyNode = stmt.body.accept(this); // Recursively visit the body
        doWhileNode.addChild(bodyNode);
        
        // Add the while condition
        TreeNode whileNode = new TreeNode("while");
        doWhileNode.addChild(whileNode);
        
        // Add the condition of the while loop
        TreeNode conditionNode = stmt.condition.accept(this); // Recursively visit the condition
        doWhileNode.addChild(conditionNode);
        
        return doWhileNode;
    }
    
    
    @Override
    public TreeNode visitAssignExpr(Expr.Assign expr) {
        // Create the root node for the assignment expression
        TreeNode assignNode = new TreeNode("assign");
        
        // Add the identifier node
        TreeNode identifierNode = new TreeNode("IDENTIFIER: " + expr.name.lexeme);
        assignNode.addChild(identifierNode);
        
        // Add the equals node for the assignment operator
        TreeNode equalsNode = new TreeNode(expr.operator.lexeme);
        assignNode.addChild(equalsNode);
        
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
        TreeNode callNode = new TreeNode("call");
        
        // Add the callee (the function being called)
        TreeNode calleeNode = expr.callee.accept(this);
        callNode.addChild(calleeNode);
        
        // If there are arguments, add them to the tree
        if (!expr.arguments.isEmpty()) {
            TreeNode argumentsNode = new TreeNode("arguments");
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
        // Create the root node for the get expression
        TreeNode getNode = new TreeNode("get");
        
        // Create a node for the object (the "left-hand side" of the get)
        TreeNode objectNode = expr.object.accept(this);
        getNode.addChild(objectNode);
        
        // Create a node for the property (the "right-hand side" of the get)
        TreeNode propertyNode = new TreeNode("property: " + expr.name.lexeme);
        getNode.addChild(propertyNode);
        
        return getNode;
    }
    
    
    @Override
    public TreeNode visitGroupingExpr(Expr.Grouping expr) {
        // Create the root node for the grouping expression
        TreeNode groupingNode = new TreeNode("group");
        
        // Recursively visit the grouped expression
        TreeNode innerExpressionNode = expr.expression.accept(this);
        
        // Add the grouped expression as a child of the grouping node
        groupingNode.addChild(innerExpressionNode);
        
        return groupingNode;
    }
    
    
    @Override
    public TreeNode visitLiteralExpr(Expr.Literal expr) {
        TreeNode literalNode = new TreeNode("LITERAL: " + (expr.value == null ? "null" : expr.value.toString()));
        return literalNode;
    }
    
    @Override
    public TreeNode visitLogicalExpr(Expr.Logical expr) {
        // Create the root node for the logical expression
        TreeNode logicalNode = new TreeNode(expr.operator.lexeme);
        
        // Create and add the left operand node
        TreeNode leftNode = expr.left.accept(this);  // Recursively visit the left expression
        logicalNode.addChild(leftNode);
        
        // Create and add the right operand node
        TreeNode rightNode = expr.right.accept(this);  // Recursively visit the right expression
        logicalNode.addChild(rightNode);
        
        return logicalNode;
    }
    
    @Override
    public TreeNode visitSetExpr(Expr.Set expr) {
        TreeNode setNode = new TreeNode("set");
        
        // Create and add the object node
        TreeNode objectNode = new TreeNode("object");
        objectNode.addChild(expr.object.accept(this)); // Recursively accept the object
        setNode.addChild(objectNode);
        
        // Create and add the property node
        TreeNode propertyNode = new TreeNode("property: " + expr.name.lexeme);
        setNode.addChild(propertyNode);
        
        // Create and add the value node
        TreeNode valueNode = new TreeNode("value");
        valueNode.addChild(expr.value.accept(this)); // Recursively accept the value
        setNode.addChild(valueNode);
        
        return setNode;
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
    public TreeNode visitUnaryExpr(Expr.Unary expr) {
        TreeNode unaryNode = new TreeNode("unary");
        
        TreeNode operatorNode = new TreeNode("operator: " + expr.operator.lexeme);
        unaryNode.addChild(operatorNode);        
        
        TreeNode rightNode = expr.right.accept(this); // Recursively visit the right operand
        unaryNode.addChild(rightNode);
        return unaryNode;
    }
    
    @Override
    public TreeNode visitVariableExpr(Expr.Variable expr) {
        TreeNode variableNode = new TreeNode("IDENTIFIER: " + expr.name.lexeme);
        return variableNode;
    }

    @Override
    public TreeNode visitUnitExpr(Expr.Unit expr) {
        TreeNode unitNode = new TreeNode("UNIT: " + expr.number.lexeme + expr.unit.lexeme);
        return unitNode;
    }
}