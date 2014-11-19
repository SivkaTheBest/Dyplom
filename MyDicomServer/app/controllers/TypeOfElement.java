package controllers;

public enum TypeOfElement {
    DIR("dir"),
    FILE("file");

    private String type;

    TypeOfElement(String type) {
        this.type = type;
    }
}
