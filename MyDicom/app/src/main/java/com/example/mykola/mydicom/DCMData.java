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

    public boolean isLoaded() {
        return isLoaded;
    }

    private void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    private int brightness;
    private double contrast;
    private boolean isInverted;
    private boolean isLoaded;

    public DCMData() {
        setDefault();
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
        if(this.contrast > 2)
            this.contrast = 2;
    }

    public void minusContrast(double contrast) {
        this.contrast -= contrast;
        if(this.contrast < 0)
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

    private List<String> tags = new LinkedList<String>();

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
            return Bitmap.createBitmap(workBuffer, sizeX, sizeY, Bitmap.Config.ARGB_8888);
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
            int A, R, G, B;
            int length = sizeX * sizeY;

            for (int i = 0; i < length; i++) {
                A = Color.alpha(workBuffer[i]);
                R = Color.red(workBuffer[i]);
                G = Color.green(workBuffer[i]);
                B = Color.blue(workBuffer[i]);

                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                workBuffer[i] = Color.argb(A, R, G, B);
            }
        }

        private void brightImage() {
            if (brightness != 0) {
                int length = sizeX * sizeY;
                int A, R, G, B;

                for (int i = 0; i < length; i++) {

                    A = Color.alpha(workBuffer[i]);
                    R = Color.red(workBuffer[i]);
                    G = Color.green(workBuffer[i]);
                    B = Color.blue(workBuffer[i]);

                    R += brightness;
                    if (R > 255) {
                        R = 255;
                    } else if (R < 0) {
                        R = 0;
                    }

                    G += brightness;
                    if (G > 255) {
                        G = 255;
                    } else if (G < 0) {
                        G = 0;
                    }

                    B += brightness;
                    if (B > 255) {
                        B = 255;
                    } else if (B < 0) {
                        B = 0;
                    }

                    workBuffer[i] = Color.argb(A, R, G, B);
                }
            }
        }
    }
}
