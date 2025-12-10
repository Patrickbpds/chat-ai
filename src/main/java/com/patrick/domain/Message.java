package com.patrick.domain;

import java.util.Objects;

public class Message {
    private final Role role;
    private final String text;

    public Message(Role role, String text) {
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        if (text == null) throw new IllegalArgumentException("Text cannot be null");

        this.role = role;
        this.text = text;
    }

    public Role role() {
        return role;
    }

    public String text() {
        return text;
    }

    @Override public String toString() { return role.name() + ": "+ text; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message msg = (Message) o;
        return role == msg.role && text.equals(msg.text);
    }
    @Override public int hashCode() { return Objects.hash(role, text);}

}
