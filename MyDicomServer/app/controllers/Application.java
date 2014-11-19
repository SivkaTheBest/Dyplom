package controllers;

import com.google.gson.Gson;

import org.apache.commons.lang.StringUtils;

import play.mvc.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Application extends Controller {

    public static final String ROOT_DIR = "D:\\data\\";

    public static void index() {
        render();
    }

    public static void findPatients() {
        Patient[] patients = new Patient[4];

        patients[0] = new Patient("ab82", "Микола Сопушинський");
        patients[1] = new Patient("cd39", "Адрій Лозовий");
        patients[2] = new Patient("fg49", "Юрій Черненко");
        patients[3] = new Patient("jq82", "Василь Пупкін");

        Gson json = new Gson();
        renderJSON(json.toJson(patients));
    }

    public static void findPatientData(String id, String path) {
        if(StringUtils.isEmpty(path))
            path = "/";
        if(!path.endsWith("\\"))
            path += "\\";

        path =  path.replace("/", "\\");

        String dataDir = ROOT_DIR + path;

        File folder = new File(dataDir);
        if(folder.exists() && folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            FileElement[] listOfElements = new FileElement[listOfFiles.length];

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    listOfElements[i] = new FileElement(TypeOfElement.FILE, listOfFiles[i].getName(), path + listOfFiles[i].getName());
                    System.out.println("File " + listOfFiles[i].getName());
                } else if (listOfFiles[i].isDirectory()) {
                    System.out.println("Directory " + listOfFiles[i].getName());
                    listOfElements[i] = new FileElement(TypeOfElement.DIR, listOfFiles[i].getName(), path + listOfFiles[i].getName());
                }
            }

            Gson json = new Gson();
            renderJSON(json.toJson(listOfElements));
        }
    }

}