package controllers;

public class FileElement {
    public FileElement(TypeOfElement type, String name, String path) {
        this.type = type;
        this.name = name;
        this.path = path;
    }

    private TypeOfElement type;
    private String name;
    private String path;
}
