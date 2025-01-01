package com.guava;

import java.util.List;

abstract class Stmt {
 interface Visitor<T> {
    T visitBlockStmt(Block stmt);
    T visitExpressionStmt(Expression stmt);
    T visitFunctionStmt(Function stmt);
    T visitIfStmt(If stmt);
    T visitReturnStmt(Return stmt);
    T visitVarStmt(Var stmt);
    T visitWhileStmt(While stmt);
    T visitDoWhileStmt(DoWhile stmt);
    T visitPrintStmt(Print stmt);
    T visitLayoutStmt(Layout stmt);
    T visitStyleStmt(Style stmt);
    T visitEventStmt(Event stmt);
    T visitAddStmt(Add stmt);
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
  static class Function extends Stmt {
    Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final List<Token> params;
    final List<Stmt> body;
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
  static class Var extends Stmt {
    Var(Token name, Expr initializer, List<Modifier> modifiers) {
      this.name = name;
      this.initializer = initializer;
      this.modifiers = modifiers;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
    final List<Modifier> modifiers;
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
  static class Style extends Stmt {
    Style(Expr styleLiteral) {
      this.styleLiteral = styleLiteral;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitStyleStmt(this);
    }

    final Expr styleLiteral;
  }
  static class Event extends Stmt {
    Event(Expr eventLiteral) {
      this.eventLiteral = eventLiteral;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitEventStmt(this);
    }

    final Expr eventLiteral;
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
