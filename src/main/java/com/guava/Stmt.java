package com.guava;

import java.util.List;

abstract class Stmt {
 interface Visitor<T> {
    T visitMainStmt(Main stmt);
    T visitFunctionStmt(Function stmt);
    T visitVarStmt(Var stmt);
    T visitExpressionStmt(Expression stmt);
    T visitWhileStmt(While stmt);
    T visitDoWhileStmt(DoWhile stmt);
    T visitIfStmt(If stmt);
    T visitPrintStmt(Print stmt);
    T visitReturnStmt(Return stmt);
    T visitBreakStmt(Break stmt);
    T visitContinueStmt(Continue stmt);
    T visitBlockStmt(Block stmt);
    T visitLayoutStmt(Layout stmt);
    T visitAddStmt(Add stmt);
  }
  static class Main extends Stmt {
    Main(Token varType, Token paramName, List<Stmt> body) {
      this.varType = varType;
      this.paramName = paramName;
      this.body = body;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitMainStmt(this);
    }

    final Token varType;
    final Token paramName;
    final List<Stmt> body;
  }
  static class Function extends Stmt {
    Function(Token returnType,
          Token modifier,
          Token name,
          List<Token> varTypes,
          List<Token> params,
          List<Stmt> body) {
      this.returnType = returnType;
      this.modifier = modifier;
      this.name = name;
      this.varTypes = varTypes;
      this.params = params;
      this.body = body;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token returnType;
    final Token modifier;
    final Token name;
    final List<Token> varTypes;
    final List<Token> params;
    final List<Stmt> body;
  }
  static class Var extends Stmt {
    Var(Token varType, Token modifier, Token name, Expr initializer) {
      this.varType = varType;
      this.modifier = modifier;
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitVarStmt(this);
    }

    final Token varType;
    final Token modifier;
    final Token name;
    final Expr initializer;
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
  static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }
  static class DoWhile extends Stmt {
    DoWhile(Stmt body, Expr condition) {
      this.body = body;
      this.condition = condition;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitDoWhileStmt(this);
    }

    final Stmt body;
    final Expr condition;
  }
  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }
  static class Print extends Stmt {
    Print(List<Expr> arguments) {
      this.arguments = arguments;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final List<Expr> arguments;
  }
  static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitReturnStmt(this);
    }

    final Token keyword;
    final Expr value;
  }
  static class Break extends Stmt {
    Break() { }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitBreakStmt(this);
    }

  }
  static class Continue extends Stmt {
    Continue() { }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitContinueStmt(this);
    }

  }
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
  static class Layout extends Stmt {
    Layout(Token type, List<Expr> arguments) {
      this.type = type;
      this.arguments = arguments;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitLayoutStmt(this);
    }

    final Token type;
    final List<Expr> arguments;
  }
  static class Add extends Stmt {
    Add(Expr name) {
      this.name = name;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitAddStmt(this);
    }

    final Expr name;
  }

  abstract <T> T accept(Visitor<T> visitor);
}
