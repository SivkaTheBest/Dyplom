package com.example.mykola.mydicom;

import android.graphics.Bitmap;
import android.graphics.Color;

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

    public Bitmap getFrame(int number) {
        return frames.get(number).getBitmap();
    }

    public int getBrightness() {
        return brightness;
    }

    public String getFileName() {
        return fileName;
    }

    public void addBrightness(int brightness) {
        this.brightness += brightness;
        if (this.brightness >= 260)
            this.brightness = 260;

    }

    public void minusBrightness(int brightness) {
        this.brightness -= brightness;
        if (this.brightness <= -260)
            this.brightness = -260;
    }

    public void addContrast(double contrast) {
        this.contrast += contrast;
        if (this.contrast > 2)
            this.contrast = 2;
    }

    public void minusContrast(double contrast) {
        this.contrast -= contrast;
        if (this.contrast < 0)
            this.contrast = 0;
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



    public class ImageData {
        private int[] buffer;
        private int[] workBuffer;
        private int sizeX;
        private int sizeY;


        public ImageData(int[] buffer, int sizeX, int sizeY) {
            this.buffer = buffer;
            this.workBuffer = new int[buffer.length];
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        public Bitmap getBitmap() {
            copyBuffer();
            contrastImage();
            brightImage();
            invertImage();
            rainbowImage();

            return Bitmap.createBitmap(workBuffer, sizeX, sizeY, Bitmap.Config.RGB_565);
        }

        private void copyBuffer() {
            int length = sizeX * sizeY;

            for (int i = 0; i < length; i++) {
                workBuffer[i] = buffer[i];
            }
        }

        private void invertImage() {
            if (isInverted()) {
                int length = sizeX * sizeY;

                for (int i = 0; i < length; i++) {
                    workBuffer[i] = (workBuffer[i] & 0xff000000) | ~(workBuffer[i] & 0x00ffffff);
                }
            }
        }

        private void rainbowImage() {
            float hsb[] = new float[3];

            if (isRainbowed()) {
                int length = sizeX * sizeY;
                int value;
                hsb[1] = 1f;

                for (int i = 0; i < length; i++) {
                    value = workBuffer[i] & 0x000000ff;

                    hsb[0] = (255f - (float) value) / 255;
                    hsb[2] = (float) value / 255;

                    workBuffer[i] = hsbToColor(hsb[0], hsb[2]);
                }
            }
        }

        private void contrastImage() {
            int value;
            int length = sizeX * sizeY;

            for (int i = 0; i < length; i++) {
                value = workBuffer[i] & 0x000000ff;

                value = (int) (((((value / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (value < 0) {
                    value = 0;
                } else if (value > 255) {
                    value = 255;
                }

                workBuffer[i] = Color.rgb(value, value, value);
            }
        }

        private void brightImage() {
            if (brightness != 0) {
                int length = sizeX * sizeY;
                int value;

                for (int i = 0; i < length; i++) {
                    value = workBuffer[i] & 0x000000ff;

                    value += brightness;
                    if (value > 255) {
                        value = 255;
                    } else if (value < 0) {
                        value = 0;
                    }

                    workBuffer[i] = Color.rgb(value, value, value);
                }
            }
        }

        public int hsbToColor(float hue, float brightness) {
            int r = 0, g = 0, b = 0;

            double h = (hue -  StrictMath.floor(hue)) * 6.0f;
            double f = h -  StrictMath.floor(h);

            double q = brightness * (1.0f - f) * 255.0f + 0.5f;
            double t = brightness * f * 255.0f + 0.5f;

            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) t;
                    b = 0;
                    break;
                case 1:
                    r = (int) q;
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = 0;
                    break;
                case 2:
                    r = 0;
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) t;
                    break;
                case 3:
                    r = 0;
                    g = (int) q;
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) t;
                    g = 0;
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = 0;
                    b = (int) q;
                    break;
            }

            return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
        }
    }
}
