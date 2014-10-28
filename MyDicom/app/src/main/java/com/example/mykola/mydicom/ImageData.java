package com.example.mykola.mydicom;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Mykola on 28.10.2014.
 */
public class ImageData {
  /*  private int[] buffer;
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

    private void rainbowImage() {
        float hsb[] = new float[3];

        if (isRainbowed()) {
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
    }*/
}