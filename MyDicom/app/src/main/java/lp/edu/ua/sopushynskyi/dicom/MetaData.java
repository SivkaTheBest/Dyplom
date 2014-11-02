package lp.edu.ua.sopushynskyi.dicom;

public class MetaData {
    private String manufacturer;
    private String manufacturerModel;

    private String patientName;
    private String patientID;
    private String procedure;
    private String date;
    private String patientAge;
    private String patientWeight;

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getManufacturerModel() {
        return manufacturerModel;
    }

    public void setManufacturerModel(String manufacturerModel) {
        this.manufacturerModel = manufacturerModel;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientWeight() {
        return patientWeight;
    }

    public void setPatientWeight(String patientWeight) {
        this.patientWeight = patientWeight;
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append(String.format("Пацієнт  : [%s] %s\n", patientID, patientName));
        bld.append(String.format("Дата     : %s\n", date));
        bld.append(String.format("Процедура: %s\n", procedure));
        bld.append(String.format("Прилад   : %s %s", manufacturer, manufacturerModel));
        return bld.toString();
    }
}