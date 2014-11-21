package controllers;

public class FileElement {
    public FileElement(TypeOfElement type, String name, String path, String date) {
        this.type = type;
        this.name = name;
        this.path = path;
        this.date = date;
    }

    private TypeOfElement type;
    private String name;
    private String path;
    private String date;
}
