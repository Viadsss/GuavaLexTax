package com.guava;

public class EventAction {
    public final Token name;
    public final Stmt block;

    public EventAction(Token name, Stmt block) {
        this.name = name;
        this.block = block;
    }
}

