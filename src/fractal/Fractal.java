package fractal;

/**
 *
 * @author Daniel
 */
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Calendar;
public class Fractal {

    public static void main(String[] args) throws Exception {
        int width = 1000;
        int height = 1000;
        int tasks = 1;
        double zoom = 1.0;
        double moveX = 0.0;
        double moveY = 0.0;
        boolean quiet = false;
        String imageName = "zad21.png";
        boolean coord = false;
        int maxIterations = 1000;
        for (int i = 0; i < args.length; i++) {
            if ("-size".equals(args[i]) || "-s".equals(args[i])) {
                String size = args[i + 1];
                char[] temp = size.toCharArray();
                char[] temp1;
                char[] temp2;
                for (int j = 0; j < temp.length; j++) {
                    if (temp[j] == 'x') {
                        temp1 = new char[j];
                        temp2 = new char[temp.length - j - 1];
                        System.arraycopy(temp, 0, temp1, 0, j);
                        System.arraycopy(temp, j + 1, temp2, 0, temp.length - j - 1);

                        width = Integer.parseInt(new String(temp1));
                        height = Integer.parseInt(new String(temp2));
                    }
                }
            }
            if ("-rect".equals(args[i]) || "-r".equals(args[i])) {

            }
            if ("-moveX".equals(args[i])) {
                moveX = Double.parseDouble(args[i + 1]);
            }
            if ("-moveY".equals(args[i])) {
                moveY = Double.parseDouble(args[i + 1]);
            }
            if ("-zoom".equals(args[i]) || "-z".equals(args[i])) {
                zoom = Double.parseDouble(args[i + 1]);
            }
            if ("-tasks".equals(args[i]) || "-t".equals(args[i])) {
                tasks = Integer.parseInt(args[i + 1]);
            }
            if ("-output".equals(args[i]) || "-o".equals(args[i])) {
                imageName = args[i + 1];
            }
            if ("-quiet".equals(args[i]) || "-q".equals(args[i])) {
                quiet = true;
            }
            if ("-coord".equals(args[i]) || "-c".equals(args[i])) {
                coord = true;
            }
            if ("-maxIterations".equals(args[i]) || "-m".equals(args[i])) {
                maxIterations = Integer.parseInt(args[i + 1]);
            }
        }
        System.out.println("availableProcessors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("width: " + width + " height: " + height);
        System.out.println("maxIterations: " + maxIterations);
        System.out.println("threads: " + tasks);
        System.out.println("zoom: " + zoom + " , moveX: " + moveX + " , moveY: " + moveY);

        imageProcessingWithThreadsSync(width, height, maxIterations, tasks, imageName, coord, zoom, moveX, moveY);

    }

    private static void imageProcessingWithThreadsSync(int width, int height, int maxIterations, int tasks, String imageName, boolean coord, double zoom, double moveX, double moveY) throws IOException {
        ConcurrentFractal.ConcurrencyContext context = new ConcurrentFractal.ConcurrencyContext(height);
        ExecutorService executor = Executors.newFixedThreadPool(tasks);

        long startTime = Calendar.getInstance().getTimeInMillis();
        for (int i = 0; i < tasks; i++) {
            Runnable worker = new ConcurrentFractal(context, width, height, maxIterations, imageName, coord, zoom, moveX, moveY);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        
        long endTime = Calendar.getInstance().getTimeInMillis();
        long duration = (endTime - startTime);
        System.out.println("Duration in milliseconds: " + duration);
        System.out.println("Calculating finished. Starting writing to file...");
        ConcurrentFractal.writeImage();

    }
}
