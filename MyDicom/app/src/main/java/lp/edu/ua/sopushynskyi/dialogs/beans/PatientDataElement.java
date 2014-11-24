package lp.edu.ua.sopushynskyi.dialogs.beans;

public class PatientDataElement {

    private String name;
    private TypeOfElement type;
    private String path;
    private String date;

    public PatientDataElement(String name, TypeOfElement type, String path, String date) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeOfElement getType() {
        return type;
    }

    public void setType(TypeOfElement type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
