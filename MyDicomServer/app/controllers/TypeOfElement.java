package controllers;

public enum TypeOfElement {
    DCM("DCM"),
    FILE("FILE");

    private String type;

    TypeOfElement(String type) {
        this.type = type;
    }
}
