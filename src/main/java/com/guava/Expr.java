package com.guava;

import java.util.List;

abstract class Expr {
 interface Visitor<T> {
    T visitAssignExpr(Assign expr);
    T visitBinaryExpr(Binary expr);
    T visitCallExpr(Call expr);
    T visitGetExpr(Get expr);
    T visitSetExpr(Set expr);
    T visitGroupingExpr(Grouping expr);
    T visitLiteralExpr(Literal expr);
    T visitLogicalExpr(Logical expr);
    T visitUnaryExpr(Unary expr);
    T visitPostfixExpr(Postfix expr);
    T visitVariableExpr(Variable expr);
    T visitReadExpr(Read expr);
    T visitCompExpr(Comp expr);
    T visitStyleExpr(Style expr);
    T visitEventExpr(Event expr);
  }
  static class Assign extends Expr {
    Assign(Token name, Token operator, Expr value) {
      this.name = name;
      this.operator = operator;
      this.value = value;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Token operator;
    final Expr value;
  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
  }
  static class Get extends Expr {
    Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitGetExpr(this);
    }

    final Expr object;
    final Token name;
  }
  static class Set extends Expr {
    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitSetExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }
  static class Literal extends Expr {
    Literal(Object value, String type) {
      this.value = value;
      this.type = type;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
    final String type;
  }
  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
  static class Postfix extends Expr {
    Postfix(Expr left, Token operator) {
      this.left = left;
      this.operator = operator;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitPostfixExpr(this);
    }

    final Expr left;
    final Token operator;
  }
  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }
  static class Read extends Expr {
    Read() { }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitReadExpr(this);
    }

  }
  static class Comp extends Expr {
    Comp(Token name,
          Token type,
          List<Expr> arguments,
          List<Stmt> methods,
          List<Stmt> body) {
      this.name = name;
      this.type = type;
      this.arguments = arguments;
      this.methods = methods;
      this.body = body;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitCompExpr(this);
    }

    final Token name;
    final Token type;
    final List<Expr> arguments;
    final List<Stmt> methods;
    final List<Stmt> body;
  }
  static class Style extends Expr {
    Style(List<Token> arguments) {
      this.arguments = arguments;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitStyleExpr(this);
    }

    final List<Token> arguments;
  }
  static class Event extends Expr {
    Event(List<EventAction> actions) {
      this.actions = actions;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitEventExpr(this);
    }

    final List<EventAction> actions;
  }

  abstract <T> T accept(Visitor<T> visitor);
}
