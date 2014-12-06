package lp.edu.ua.sopushynskyi.dicom;

import android.graphics.Bitmap;

import com.imebra.dicom.CodecFactory;
import com.imebra.dicom.ColorTransformsFactory;
import com.imebra.dicom.DataSet;
import com.imebra.dicom.DrawBitmap;
import com.imebra.dicom.Image;
import com.imebra.dicom.ModalityVOILUT;
import com.imebra.dicom.Stream;
import com.imebra.dicom.StreamReader;
import com.imebra.dicom.TransformsChain;
import com.imebra.dicom.VOILUT;

import java.util.LinkedList;
import java.util.List;

public class DCMData {

    List<ImageData> frames = new LinkedList<ImageData>();
    private int brightness;
    private int currentFrame;
    private double contrast;
    private ColorSchema schema;
    private boolean isLoaded;
    private String fileName;
    private MetaData metaData;

    public DCMData() {
        setDefault();
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setContrast(float contrast) {
        this.contrast = contrast;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    private void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public void setDefault() {
        currentFrame = 0;
        brightness = 0;
        contrast = 1;
        schema = ColorSchema.NORMAL;
    }

    public void loadDCM(String fileName) {
        frames.clear();
        metaData = new MetaData();

        setLoaded(true);

        Stream stream = new Stream();
        stream.openFileRead(fileName);
        DataSet dataSet = CodecFactory.load(new StreamReader(stream), 256);
        int framesSize = 0;

        String manufacturer = dataSet.getString(0x0008, 0, 0x0070, 0);
        String manufacturerModel = dataSet.getString(0x0008, 0, 0x1090, 0);
        String patientName = dataSet.getString(0x0010, 0, 0x0010, 0);
        String patientID = dataSet.getString(0x0010, 0, 0x0020, 0);
        String patientAge = dataSet.getString(0x0010, 0, 0x1010, 0);
        String patientWeight = dataSet.getString(0x0010, 0, 0x1030, 0);
        String procedure = dataSet.getString(0x0032, 0, 0x1060, 0);
        String date = dataSet.getString(0x0008, 0, 0x0020, 0);
        String time = dataSet.getString(0x0008, 0, 0x0030, 0);

        metaData.setManufacturer(manufacturer);
        metaData.setManufacturerModel(manufacturerModel);
        metaData.setPatientName(patientName);
        metaData.setPatientID(patientID);
        metaData.setPatientAge(patientAge);
        metaData.setPatientWeight(patientWeight);
        metaData.setProcedure(procedure);

        if(time != null && !time.equals(""))
            metaData.setDate(date + " " + time.substring(0, 8));

        try {
            for (int i = 0; ; i++) {
                dataSet.getImage(i);
                framesSize++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < framesSize; i++) {
            Image image = dataSet.getImage(i);

            if (ColorTransformsFactory.isMonochrome(image.getColorSpace())) {
                ModalityVOILUT modalityVOILUT = new ModalityVOILUT(dataSet);
                if (!modalityVOILUT.isEmpty()) {
                    Image modalityImage = modalityVOILUT.allocateOutputImage(image, image.getSizeX(), image.getSizeY());
                    modalityVOILUT.runTransform(image, 0, 0, image.getSizeX(), image.getSizeY(), modalityImage, 0, 0);
                    image = modalityImage;
                }
            }

            TransformsChain transformsChain = new TransformsChain();
            if (ColorTransformsFactory.isMonochrome(image.getColorSpace())) {
                VOILUT voilut = new VOILUT(dataSet);

                int voilutId = voilut.getVOILUTId(0);
                if (voilutId != 0) {
                    voilut.setVOILUT(voilutId);
                } else {
                    voilut.applyOptimalVOI(image, 0, 0, image.getSizeX(), image.getSizeY());
                }
                transformsChain.addTransform(voilut);
            }


            DrawBitmap bitmap = new DrawBitmap(image, transformsChain);
            int[] buffer = new int[1024];
            int requiredSize = bitmap.getBitmap(image.getSizeX(), image.getSizeY(), 0, 0,
                    image.getSizeX(), image.getSizeY(), buffer, 0);
            buffer = new int[requiredSize];
            bitmap.getBitmap(image.getSizeX(), image.getSizeY(), 0, 0,
                    image.getSizeX(), image.getSizeY(), buffer, requiredSize);
            frames.add(new ImageData(buffer, image.getSizeX(), image.getSizeY()));
            this.fileName = fileName;
        }
    }

    public Bitmap getFrame() {
        if(frames.size() != 0) {
            return frames.get(currentFrame).getBitmap(isInverted(),
                    isRainbowed(), getContrast(), getBrightness());
        } else {
            return null;
        }
    }

    public int getBrightness() {
        return brightness;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isInverted() {
        return schema == ColorSchema.INVERSE;
    }

    public void inverse() {
        schema = ColorSchema.INVERSE;
    }

    public double getContrast() {
        return contrast;
    }

    public void rainbow() {
        schema = ColorSchema.RAINBOW;
    }

    public boolean isRainbowed() {
        return schema == ColorSchema.RAINBOW;
    }

    public String getColorSchema() {
        return schema.toString();
    }

    public String getMetaInfo() {
        return metaData.toString();
    }

    public void normal() {
        schema = ColorSchema.NORMAL;
    }

    public boolean isNormal() {
        return schema == ColorSchema.NORMAL;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }
    public int getFramesNumber() {
        return frames.size();
    }

    public boolean nextFrame() {
        currentFrame = currentFrame + 1;
        if(currentFrame >= frames.size()) {
            currentFrame = frames.size() - 1;
            return false;
        }
        return true;
    }

    public boolean prevFrame() {
        currentFrame = currentFrame - 1;
        if(currentFrame < 0) {
            currentFrame = 0;
            return false;
        }
        return true;
    }

    public String getFrameResolution() {
        if(frames.size() != 0) {
            ImageData img = frames.get(getCurrentFrame());
            return img.getSizeX() + "x" + img.getSizeY();
        } else {
            return "недоступно";
        }
    }
}
