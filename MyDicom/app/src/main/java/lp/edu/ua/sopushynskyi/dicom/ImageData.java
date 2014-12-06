package lp.edu.ua.sopushynskyi.dicom;

import android.graphics.Bitmap;

public class ImageData {
    private int[] buffer;
    private int[] workBuffer;
    private int sizeX;
    private int sizeY;

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public ImageData(int[] buffer, int sizeX, int sizeY) {
        this.buffer = buffer;
        this.workBuffer = new int[buffer.length];
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public Bitmap getBitmap(boolean isInverted, boolean isRainbowed, double contrast, int brightness) {
        copyBuffer();
        contrastImage(contrast);
        brightImage(brightness);
        invertImage(isInverted);
        rainbowImage(isRainbowed);

        return Bitmap.createBitmap(workBuffer, sizeX, sizeY, Bitmap.Config.RGB_565);
    }

    private void copyBuffer() {
        int length = sizeX * sizeY;
        System.arraycopy(buffer, 0, workBuffer, 0, length);
    }

    private void invertImage(boolean isInverted) {
        if (isInverted) {
            int length = sizeX * sizeY;

            for (int i = 0; i < length; i++) {
                workBuffer[i] = (workBuffer[i] & 0xff000000) | ~(workBuffer[i] & 0x00ffffff);
            }
        }
    }

    private void rainbowImage(boolean isRainbowed) {
        if (isRainbowed) {
            int length = sizeX * sizeY;
            int brightness;
            float hue;
            int r = 0, g = 0, b = 0;
            double h;
            double f;
            double q;
            double t;

            for (int i = 0; i < length; i++) {
                brightness = workBuffer[i] & 0x000000ff;
                hue = (255f - (float) brightness) / 255;

                h = (hue - (int)(hue)) * 6.0f;
                f = h - (int)h;

                q = brightness * (1.0f - f);
                t = brightness * f;

                switch ((int) h) {
                    case 0:
                        r = brightness;
                        g = (int) t;
                        b = 0;
                        break;
                    case 1:
                        r = (int) q;
                        g = brightness;
                        b = 0;
                        break;
                    case 2:
                        r = 0;
                        g = brightness;
                        b = (int) t;
                        break;
                    case 3:
                        r = 0;
                        g = (int) q;
                        b = brightness;
                        break;
                    case 4:
                        r = (int) t;
                        g = 0;
                        b = brightness;
                        break;
                    case 5:
                        r = brightness;
                        g = 0;
                        b = (int) q;
                        break;
                }

                workBuffer[i] = 0xff000000 | (r << 16) | (g << 8) | (b << 0);
            }
        }
    }

    private void contrastImage(double contrast) {
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

            workBuffer[i] = 0xff000000 | (value << 16) | (value << 8) | (value << 0);
        }
    }

    private void brightImage(int brightness) {
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

                workBuffer[i] = 0xff000000 | (value << 16) | (value << 8) | (value << 0);
            }
        }
    }

}