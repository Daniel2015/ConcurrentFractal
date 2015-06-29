package fractal;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author Daniel
 */
public class ConcurrentFractal implements Runnable {

    private static int width;
    private static int height;
    private static int maxIterations;
    private static int[] colors;
    private static String imageName;
    private static boolean coord;
    private static double zoom;
    private static double moveX;
    private static double moveY;
    private static BufferedImage image;
    private static final int white = 16777215;
    private static final int black = 0;
    private final ConcurrencyContext context;

    public ConcurrentFractal(ConcurrencyContext context, int width, int height, int maxIterations, String imageName, boolean coord, double zoom, double moveX, double moveY) {
        if (context == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        this.context = context;
        ConcurrentFractal.width = width;
        ConcurrentFractal.height = height;
        ConcurrentFractal.maxIterations = maxIterations;
        ConcurrentFractal.coord = coord;
        ConcurrentFractal.zoom = zoom;
        ConcurrentFractal.moveX = moveX;
        ConcurrentFractal.moveY = moveY;
        ConcurrentFractal.imageName = imageName;
        ConcurrentFractal.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ConcurrentFractal.colors = new int[maxIterations];
        for (int i = 0; i < maxIterations; i++) {
            ConcurrentFractal.colors[i] = Color.HSBtoRGB(i / 256f, 1, i / (i + 8f));
        }
    }

    @Override
    public void run() {
        while (true) {
            int row;
            synchronized (context) {
                if (context.isFullyProcessed()) {
                    break;
                }
                row = context.nextRowNum();
            }
            for (int col = 0; col < ConcurrentFractal.width; col++) {
                Complex C = new Complex((((col - ConcurrentFractal.width / 2) * 4.0) / (zoom * ConcurrentFractal.width)) + moveX, (((row - ConcurrentFractal.height / 2) * 4.0) / (zoom * ConcurrentFractal.height)) + moveY);
                Complex Z = new Complex(0, 0);
                int iteration = 0;
                while (Z.getReal() * Z.getReal() + Z.getImaginary() * Z.getImaginary() < 4 && iteration < maxIterations) {
                    Z = Z.add(Z.exp());
                    Z = Z.multiply(Z);
                    Z = Z.add(C);
                    iteration++;
                }
                if (iteration < ConcurrentFractal.maxIterations) {
                    float smoothcolor = (float) (iteration - (Math.log(Math.log(Z.abs())) - Math.log(Math.log(maxIterations))) / Math.log(2));
                    float color = Color.HSBtoRGB(smoothcolor / 256f, 1, smoothcolor / (smoothcolor + 8f));
                    image.setRGB(col, row, (int) color);
                } else {
                    image.setRGB(col, row, black);
                }
            }
        }
    }

    public static void writeImage() throws IOException {
        ImageIO.write(image, "png", new File(imageName));
    }

    public static int getRed(float rgb) {
        int red = (int) (rgb % 0x1000000 / 0x10000);
        return red;
    }

    public static int getGreen(float rgb) {
        int green = (int) (rgb % 0x10000 / 0x100);
        return green;
    }

    public static int getBlue(float rgb) {
        int blue = (int) (rgb % 0x100);
        return blue;
    }

    public static class ConcurrencyContext {

        private final int rowCount;
        private int nextRow = 0;

        public ConcurrencyContext(int rowCount) {
            this.rowCount = rowCount;
        }

        public synchronized int nextRowNum() {
            if (isFullyProcessed()) {
                throw new IllegalStateException("Already fully processed");
            }
            return nextRow++;
        }

        public synchronized boolean isFullyProcessed() {
            return nextRow == rowCount;
        }
    }
}
