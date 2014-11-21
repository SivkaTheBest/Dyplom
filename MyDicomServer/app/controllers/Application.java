package controllers;

import com.google.gson.Gson;

import org.apache.commons.lang.StringUtils;

import play.mvc.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class Application extends Controller {

    public static final String ROOT_DIR = "D:\\data\\";
    public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

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
        if(StringUtils.isBlank(path))
            path = "/";
        path =  path.replace("/", "\\");
        if(!path.endsWith("\\")) {
            path += "\\";
        }

        String dataDir = ROOT_DIR + path;

        File folder = new File(dataDir);
        if(folder.exists() && folder.isDirectory()) {
            int length = 0;
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    length++;
                }
            }

            FileElement[] listOfElements = new FileElement[length];
            int j = 0, i = 0;
            for (i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].getName().toUpperCase().endsWith(".DCM")) {
                    System.out.println("Directory " + listOfFiles[i].getName());

                    listOfElements[j] = new FileElement(TypeOfElement.DCM, listOfFiles[i].getName(), path + listOfFiles[i].getName(), sdf.format(listOfFiles[i].lastModified()));
                    j++;
                } else if (listOfFiles[i].isFile()) {
                    listOfElements[j] = new FileElement(TypeOfElement.FILE, listOfFiles[i].getName(), path + listOfFiles[i].getName(), sdf.format(listOfFiles[i].lastModified()));
                    System.out.println("File " + listOfFiles[i].getName());
                    j++;
                }
            }

            Gson json = new Gson();
            renderJSON(json.toJson(listOfElements));
        }
    }

    public static void getPatientImage(String id, String filePath) {
        if(StringUtils.isBlank(filePath))
            filePath = "/";
        filePath =  filePath.replace("/", "\\");
        if(!filePath.endsWith("\\")) {
            filePath += "\\";
        }

        String dataFile = ROOT_DIR + filePath;

        File file = new File(dataFile);
        if(file.exists()) {
            renderBinary(file, id + " " + sdf.format(file.lastModified()) + ".dcm");
        } else {
            notFound();
        }


    }

}