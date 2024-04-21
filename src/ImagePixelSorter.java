/**
 * ImagePixelSorter.java
 * 
 * This class provides methods to sort the pixels of an image based on color intensity and HSV (Hue, Saturation, Value) values.
 * It is designed to enhance visual appeal by organizing pixels into a gradient-like order. The class supports images with
 * non-transparent pixels, applying sorting that considers both RGB values and HSV color properties for better visual perception.
 * 
 * @author Citizen Cyrus
 * @version 1.0
 * @date 2024-04-21
 * @file ImagePixelSorter.java
 * @license The Unlicense
 */
package imagetools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ImagePixelSorter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ImagePixelSorter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Processes an image to sort its pixels by color intensity and hue, saturation, and value (HSV).
     * Only non-transparent pixels are sorted and used to create a new image.
     * This method orchestrates the extraction, sorting, and recompilation of image pixels.
     * 
     * @param image The BufferedImage to be processed.
     * @return A new BufferedImage with sorted non-transparent pixels.
     * @throws IllegalArgumentException if the provided image is null or has non-positive dimensions.
     */
    public static BufferedImage processImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("The provided image cannot be null.");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("The provided image must have non-zero dimensions.");
        }
        
        // Extract non-transparent pixels only for processing.
        Integer[] pixels = extractPixels(image, width, height);
        if (pixels.length == 0) {
            return createEmptyImage(width, height);
        }
        
        // Sort pixels first by RGB values then by HSV to enhance color perception.
        sortPixelsByIntensityAndHSV(pixels);
        
        // Reassemble the image from sorted pixels.
        return createSortedImage(pixels, width);
    }

    /**
     * Extracts non-transparent pixels from a BufferedImage.
     * 
     * @param image The source BufferedImage.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return An array of pixel ARGB values, excluding fully transparent ones.
     */
    private static Integer[] extractPixels(BufferedImage image, int width, int height) {
        Integer[] pixels = new Integer[width * height];
        int pixelCount = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = image.getRGB(j, i);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha != 0) {
                    pixels[pixelCount++] = pixel;
                }
            }
        }
        return Arrays.copyOf(pixels, pixelCount);
    }

    /**
     * Sorts an array of pixel ARGB values based on their HSV values and RGB intensity.
     * 
     * @param pixels The array of pixel ARGB values to sort.
     */
    private static void sortPixelsByIntensityAndHSV(Integer[] pixels) {
        Arrays.sort(pixels, (p1, p2) -> {
            int[] rgb1 = getRGB(p1);
            int[] rgb2 = getRGB(p2);
            for (int i = 0; i < 3; i++) {
                if (rgb2[i] != rgb1[i]) {
                    return rgb2[i] - rgb1[i];
                }
            }
            float[] hsv1 = rgbToHsv(rgb1[0], rgb1[1], rgb1[2]);
            float[] hsv2 = rgbToHsv(rgb2[0], rgb2[1], rgb2[2]);
            return Float.compare(hsv2[2], hsv1[2]);
        });
    }

    /**
     * Creates a new BufferedImage from sorted pixel values, adjusted for image width and new height.
     * This method fills the new image from left to right, top to bottom, with the sorted pixels.
     * Remaining space is filled with white pixels.
     * 
     * @param pixels An array of sorted pixel values.
     * @param width The width of the resulting image.
     * @return A BufferedImage with sorted pixels.
     */
    private static BufferedImage createSortedImage(Integer[] pixels, int width) {
        int newHeight = pixels.length / width;
        if (pixels.length % width != 0) {
            newHeight += 1;
        }

        BufferedImage sortedImage = new BufferedImage(width, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = sortedImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, newHeight);
        g2d.dispose();

        int pixelIndex = 0;
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < width; x++) {
                if (pixelIndex < pixels.length) {
                    sortedImage.setRGB(x, y, pixels[pixelIndex++] | 0xFF000000);
                }
            }
        }
        return sortedImage;
    }

    /**
     * Creates an empty white image of specified dimensions.
     * 
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A new, blank (white) BufferedImage.
     */
    private static BufferedImage createEmptyImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return image;
    }

    /**
     * Converts a pixel's ARGB value into an array of RGB components.
     * 
     * @param pixel The integer pixel value containing ARGB data.
     * @return An array containing the red, green, and blue components of the pixel.
     */
    private static int[] getRGB(int pixel) {
        return new int[]{
            (pixel >> 16) & 0xFF, // Red
            (pixel >> 8) & 0xFF,  // Green
            pixel & 0xFF          // Blue
        };
    }

    /**
     * Converts RGB values to an array of HSV (Hue, Saturation, Value) values.
     * This is useful for sorting colors in terms of human color perception.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @return An array containing the HSV values of the color.
     */
    private static float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float hue = calculateHue(rf, gf, bf, max, delta);
        float saturation = (max == 0) ? 0 : (delta / max);
        float value = max * 100;

        return new float[]{hue, saturation * 100, value};
    }

    /**
     * Calculates the hue component for the HSV color model based on normalized RGB components.
     * This method determines the hue angle in degrees based on which color component is dominant.
     * 
     * @param rf Normalized red component.
     * @param gf Normalized green component.
     * @param bf Normalized blue component.
     * @param max The maximum value among rf, gf, and bf.
     * @param delta The difference between the max and min RGB components.
     * @return The calculated hue value in degrees.
     */
    private static float calculateHue(float rf, float gf, float bf, float max, float delta) {
        float hue;
        
        if (delta == 0) {
            return 0;
        }

        if (rf == max) {
            hue = 60 * ((gf - bf) / delta);
        } else if (gf == max) {
            hue = 120 + 60 * ((bf - rf) / delta);
        } else {
            hue = 240 + 60 * ((rf - gf) / delta);
        }

        if (hue < 0) {
            hue += 360;
        }

        return hue;
    }
}
