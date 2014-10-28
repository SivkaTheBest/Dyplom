package com.example.mykola.mydicom;

public enum ColorSchema {
    NORMAL("норма"),
    INVERSE("негатив"),
    RAINBOW("псевдо");

    public String name;

    ColorSchema(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
