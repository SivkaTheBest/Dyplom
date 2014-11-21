package lp.edu.ua.sopushynskyi.dialogs.utils;

public class PatientElement {
    private String id;
    private String name;

    public PatientElement(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return  String.format("[%s] %s", getId(), getName());
    }
}