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
    T visitComponentExpr(Component expr);
    T visitStyleLiteralExpr(StyleLiteral expr);
    T visitStylePropertyExpr(StyleProperty expr);
    T visitEventLiteralExpr(EventLiteral expr);
    T visitEventPropertyExpr(EventProperty expr);
    T visitUnitExpr(Unit expr);
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
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
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
  static class Component extends Expr {
    Component(Token identifier,
          Token type,
          List<Expr> arguments,
          List<Stmt> methods,
          List<Stmt> body) {
      this.identifier = identifier;
      this.type = type;
      this.arguments = arguments;
      this.methods = methods;
      this.body = body;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitComponentExpr(this);
    }

    final Token identifier;
    final Token type;
    final List<Expr> arguments;
    final List<Stmt> methods;
    final List<Stmt> body;
  }
  static class StyleLiteral extends Expr {
    StyleLiteral(List<Expr.StyleProperty> properties) {
      this.properties = properties;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitStyleLiteralExpr(this);
    }

    final List<Expr.StyleProperty> properties;
  }
  static class StyleProperty extends Expr {
    StyleProperty(Token name, List<Expr> values) {
      this.name = name;
      this.values = values;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitStylePropertyExpr(this);
    }

    final Token name;
    final List<Expr> values;
  }
  static class EventLiteral extends Expr {
    EventLiteral(List<Expr.EventProperty> properties) {
      this.properties = properties;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitEventLiteralExpr(this);
    }

    final List<Expr.EventProperty> properties;
  }
  static class EventProperty extends Expr {
    EventProperty(Token name, Stmt action) {
      this.name = name;
      this.action = action;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitEventPropertyExpr(this);
    }

    final Token name;
    final Stmt action;
  }
  static class Unit extends Expr {
    Unit(Token number, Token unit) {
      this.number = number;
      this.unit = unit;
    }

    @Override
    <T> T accept(Visitor<T> visitor) {
      return visitor.visitUnitExpr(this);
    }

    final Token number;
    final Token unit;
  }

  abstract <T> T accept(Visitor<T> visitor);
}
