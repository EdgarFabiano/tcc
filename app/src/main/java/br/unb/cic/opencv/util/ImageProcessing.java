package br.unb.cic.opencv.util;

import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;

public class ImageProcessing {

    private ImageProcessing() {
        throw new UnsupportedOperationException("No " + ImageProcessing.class.getSimpleName() + " instances for you!");
    }

    public static void checkOpenCV() {
        Log.d("OpenCV_LIB",
                OpenCVLoader.initDebug() ?
                        "OpenCV successfully Loaded" : "OpenCV not Loaded");
    }

    /**
     * Applies the Gaussian filter in an Mat with the mask size of 3x3
     */
    public static Mat gaussian3(Mat mat) {
        Mat dst = new Mat();
        Imgproc.GaussianBlur(mat, dst, new Size(3, 3), 0, 0, BORDER_DEFAULT);
        return dst;
    }

    /**
     * Applies the Gaussian filter in an Mat with a generic mask size
     */
    public static Mat gaussian(Mat mat, double size) {
        Mat dst = new Mat();
        Imgproc.GaussianBlur(mat, dst, new Size(size, size), 0, 0, BORDER_DEFAULT);
        return dst;
    }

    /**
     * Converts a Mat to gray scale
     */
    public static Mat convertToGray(Mat mat) {
        Mat dst = new Mat();
        Imgproc.cvtColor(mat, dst, COLOR_BGR2GRAY);
        return dst;
    }

    /**
     * Applies the Sobel filter to a Mat, to get its borders
     */
    public static Mat sobelFilter(Mat mat) {
        Mat dst = new Mat();

        int scale = 1, delta = 0;
        Mat grad_x = new Mat(), grad_y = new Mat();
        Imgproc.Sobel(mat, grad_x, CV_16S, 1, 0, 3, scale, delta, BORDER_DEFAULT);
        Imgproc.Sobel(mat, grad_y, CV_16S, 0, 1, 3, scale, delta, BORDER_DEFAULT);

        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();
        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);

        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, dst);

        return dst;
    }

    /**
     * Applies the Canny edge detection algorithm
     */
    public static Mat canny(Mat mat) {
        Mat dst = new Mat();
        Imgproc.Canny(mat, dst, 50, 200, 5, true);
        return dst;
    }

    /**
     * Applies the standard Hough transform in an Mat
     */
    public static Mat standardHoughTransform(Mat mat) {
        Mat lines = new Mat(), result = new Mat();
        Imgproc.HoughLinesP(mat, lines, 1, Math.PI / 180, 50, 100, 50);

        List<Line> horizontals = new ArrayList<>();
        List<Line> verticals = new ArrayList<>();
        for (int x = 0; x < lines.rows(); x++) {
            double[] vec = lines.get(x, 0);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Line line = new Line(start, end);
            if (Math.abs(x1 - x2) > Math.abs(y1 - y2)) {
                horizontals.add(line);
            } else if (Math.abs(x2 - x1) < Math.abs(y2 - y1)) {
                verticals.add(line);
            }

            Imgproc.line(mat, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 0, 255), 2);
        }

        return mat;
    }

    protected Point computeIntersection(Line l1, Line l2) {
        double x1 = l1.start.x, x2 = l1.end.x, y1 = l1.start.y, y2 = l1.end.y;
        double x3 = l2.start.x, x4 = l2.end.x, y3 = l2.start.y, y4 = l2.end.y;
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

//         double angle = angleBetween2Lines(l1,l2);
//        Log.e("houghline", "angle between 2 lines = " + angle);
        Point point = new Point();
        point.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        point.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

        return point;
    }

    private static class Line {
        public Point start, end;

        private Line(Point start, Point end) {
            this.start = start;
            this.end = end;
        }
    }
}
