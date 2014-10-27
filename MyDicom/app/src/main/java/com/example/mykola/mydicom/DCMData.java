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
    private boolean isInverted;
    private boolean isLoaded;
    private List<String> tags = new LinkedList<String>();

    public DCMData() {
        setDefault();
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    private void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public void setDefault() {
        brightness = 0;
        contrast = 1;
        isInverted = false;
    }

    public void loadDCM(String fileName) {
        frames.clear();
        setLoaded(true);
        // Open the dicom file from sdcard
        Stream stream = new Stream();
        stream.openFileRead(fileName);

        // Build an internal representation of the Dicom file. Tags larger than 256 bytes
        //  will be loaded on demand from the file
        DataSet dataSet = CodecFactory.load(new StreamReader(stream), 256);
        int framesSize = 0;

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
        }
    }

    public Bitmap getFrame(int number) {
        return frames.get(number).getBitmap();
    }

    public int getBrightness() {
        return brightness;
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
        return isInverted;
    }

    public void inverse() {
        isInverted = !isInverted;
    }

    public double getContrast() {
        return contrast;
    }

    private void readTags() {

    }

    private class TagDictionary {


        private class Tag {
            private String name;
            private String tag;
        }
    }

    private class ImageData {
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
            invertImage();
            contrastImage();
            brightImage();
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

        private void rainbowImage() {
            boolean isRainbow = true;
            float hsb[] = new float[3];

            if (isRainbow) {
                int length = sizeX * sizeY;
                int value;
                hsb[1] = 1f;

                for (int i = 0; i < length; i++) {
                    value = workBuffer[i] & 0x000000ff;

                    hsb[0] = 360 - ((float)value  / 255)  * 360;
                    hsb[2] = (float)value  / 255;

                    workBuffer[i] = Color.HSVToColor(hsb);
                }
            }
        }
    }
}
