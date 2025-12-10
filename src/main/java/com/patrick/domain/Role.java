package com.patrick.domain;

public enum Role {
    USER("user"),
    MODEL("model"),
    SYSTEM("system");

private final String wire;

    Role(String wire) {
        this.wire = wire;
    }

    public String Wire() {
        return wire;
    }
}
