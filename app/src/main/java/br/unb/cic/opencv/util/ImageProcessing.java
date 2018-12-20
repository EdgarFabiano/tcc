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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.HoughLinesP;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.threshold;

public class ImageProcessing {

    private static double toleranceFactor = Math.PI / 6;

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
        GaussianBlur(mat, dst, new Size(3, 3), 0, 0, BORDER_DEFAULT);
        return dst;
    }

    /**
     * Applies the Gaussian filter in an Mat with a generic mask size
     */
    public static Mat gaussian(Mat mat, double size) {
        Mat dst = new Mat();
        GaussianBlur(mat, dst, new Size(size, size), 0, 0, BORDER_DEFAULT);
        return dst;
    }

    /**
     * Converts a Mat to gray scale
     */
    public static Mat convertToGray(Mat mat) {
        Mat dst = new Mat();
        cvtColor(mat, dst, COLOR_BGR2GRAY);
        return dst;
    }

    /**
     * Converts a Mat to color
     */
    public static Mat convertToColor(Mat mat) {
        Mat dst = new Mat();
        cvtColor(mat, dst, COLOR_GRAY2BGR);
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

        Core.addWeighted(abs_grad_x, 1, abs_grad_y, 1, 0, dst);

        threshold(dst, dst, 40, 255, Imgproc.THRESH_BINARY);

        return dst;
    }

    /**
     * Applies the Canny edge detection algorithm
     */
    public static Mat canny(Mat mat) {
        Mat dst = new Mat();
        Canny(mat, dst, 50, 200, 5, true);
        return dst;
    }

    /**
     * Applies the standard Hough transform in an Mat
     */
    public static Mat standardHoughTransform(Mat mat) {
        Mat lines = new Mat();
        int height = mat.height();
        int width = mat.width();
        int minImageDimention = Math.min(width, height);
        HoughLinesP(mat, lines, 1, 2 * Math.PI / 180, 50, minImageDimention / 20, 50);

        Scalar color = new Scalar(255, 0, 0);
        int thickness = 2;

        mat = convertToColor(mat);

        List<Line> imageLines = new ArrayList<>();

        for (int i = 0; i < lines.rows(); i++) {
            double[] vec = lines.get(i, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Line line = new Line(start, end);
            imageLines.add(line);
        }


        for (Line line : imageLines) {
            Imgproc.arrowedLine(mat, line.start, line.end, color, thickness);
        }

        return mat;
    }

    public static Mat resizeIfNecessary(Mat mat) {
        final float FINAL_SIZE = 1280f;

        int width = mat.width();
        int height = mat.height();
        Mat aux = new Mat();

        if (width > FINAL_SIZE) {
            float aspectRatio = width / FINAL_SIZE;
            Imgproc.resize(mat, aux, new Size(1280, height / aspectRatio));
            return aux;
        } else if (height > FINAL_SIZE) {
            float aspectRatio = height / FINAL_SIZE;
            Imgproc.resize(mat, aux, new Size(width / aspectRatio, 1280));
            return aux;
        }

        return mat;
    }

    public static Mat bestApproach(Mat src, Mat original, Boolean isLines) {
        Mat out = original.clone();
        int CV_THRESH_BINARY = 0;

        Mat binaryImg = new Mat();
        adaptiveThreshold(src, binaryImg, 255, ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 85, 10);
        bitwise_not (binaryImg, binaryImg);

        Mat kernel10 = getStructuringElement(MORPH_RECT, new Size(10, 10));
        Mat kernel20 = getStructuringElement(MORPH_RECT, new Size(20, 20));

        dilate(binaryImg, binaryImg, kernel20);
        erode(binaryImg, binaryImg, kernel10);

        Mat lines = new Mat();
        int height = src.height();
        int width = src.width();
        int minImageDimention = Math.min(width, height);
        HoughLinesP(binaryImg, lines, 1, 2 * Math.PI / 180, 50, minImageDimention / 3, 100);

        List<Line> imageLines = new ArrayList<>();

        for (int i = 0; i < lines.rows(); i++) {
            double[] vec = lines.get(i, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Line line = new Line(start, end);
            imageLines.add(line);
        }

        List<List<Point>> corners = computeLines(imageLines, src);

        for (int i = 0; i < corners.size(); i++) {
            Point center = new Point(0, 0);
            if (corners.get(i).size() < 4) continue;
            for (int j = 0; j < corners.get(i).size(); j++) {
                center.x += corners.get(i).get(j).x;
                center.y += corners.get(i).get(j).y;
            }
            center.x *= (1. / corners.get(i).size());
            center.y *= (1. / corners.get(i).size());
            sortCorners(corners.get(i), center);
        }

        if (!isLines) {
            for (int i = 0; i < corners.size(); i++) {
                List<Point> square = corners.get(i);
                if (square.size() >= 4) {
                    Point tl = square.get(0);
                    Point tr = square.get(1);
                    Point br = square.get(2);
                    Point bl = square.get(3);
                    int r = new Random().nextInt(256);
                    int g = new Random().nextInt(256);
                    int b = new Random().nextInt(256);
                    Imgproc.line(out, tl, tr, new Scalar(r, g, b), 3);
                    Imgproc.line(out, tl, bl, new Scalar(r, g, b), 3);
                    Imgproc.line(out, bl, br, new Scalar(r, g, b), 3);
                    Imgproc.line(out, br, tr, new Scalar(r, g, b), 3);
                }
            }
        } else {
            for (Line line : imageLines) {
                Imgproc.line(out, line.start, line.end, new Scalar(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256)), 2);
            }
        }

        return out;
    }

    private static Point computeIntersection(Line l1, Line l2) {
        double x1 = l1.start.x, x2 = l1.end.x, y1 = l1.start.y, y2 = l1.end.y;
        double x3 = l2.start.x, x4 = l2.end.x, y3 = l2.start.y, y4 = l2.end.y;
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (d < 0) {
            Point pt = new Point();
            pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

            int threshold = 10;
            if (pt.x < Math.min(x1, x2) - threshold || pt.x > Math.max(x1, x2) + threshold || pt.y < Math.min(y1, y2) - threshold || pt.y > Math.max(y1, y2) + threshold) {
                return new Point(-1, -1);
            }
            if (pt.x < Math.min(x3, x4) - threshold || pt.x > Math.max(x3, x4) + threshold || pt.y < Math.min(y3, y4) - threshold || pt.y > Math.max(y3, y4) + threshold) {
                return new Point(-1, -1);
            }
            return pt;
        } else
            return new Point(-1, -1);

    }

    /**
     * Primeiro, inicialize cada linha para estar em um grupo indefinido.
     * Para cada linha calcule a intersecção dos dois segmentos de linha (se eles não cruzarem, ignore o ponto).
     *      Se ambas as linhas estiverem indefinidas, crie um novo grupo delas.
     *      Se apenas uma linha for definida em um grupo, adicione a outra linha ao grupo.
     *      Se ambas as linhas estiverem definidas, adicione todas as linhas de um grupo ao outro grupo.
     *      Se ambas as linhas estiverem no mesmo grupo, não faça nada
     */
    private static List<List<Point>> computeLines(List<Line> lines, Mat img2) {
        int[] poly = new int[lines.size()];
        for (int i = 0; i < lines.size(); i++) poly[i] = -1;

        int curPoly = 0;
        List<List<Point>> corners = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            for (int j = i + 1; j < lines.size(); j++) {

                Point pt = computeIntersection(lines.get(i), lines.get(j));
                if (pt.x >= 0 && pt.y >= 0 && pt.x < img2.size().width && pt.y < img2.size().height) {

                    if (poly[i] == -1 && poly[j] == -1) {
                        List<Point> v = new ArrayList<>();

                        v.add(pt);
                        corners.add(v);
                        poly[i] = curPoly;
                        poly[j] = curPoly;
                        curPoly++;
                        continue;
                    }
                    if (poly[i] == -1 && poly[j] >= 0) {
                        corners.get(poly[j]).add(pt);
                        poly[i] = poly[j];
                        continue;
                    }
                    if (poly[i] >= 0 && poly[j] == -1) {
                        corners.get(poly[i]).add(pt);
                        poly[j] = poly[i];
                        continue;
                    }
                    if (poly[i] >= 0 && poly[j] >= 0) {
                        if (poly[i] == poly[j]) {
                            corners.get(poly[i]).add(pt);
                            continue;
                        }

                        for (int k = 0; k < corners.get(poly[j]).size(); k++) {
                            corners.get(poly[i]).add(corners.get(poly[j]).get(k));
                        }

                        corners.get(poly[j]).clear();
                        poly[j] = poly[i];
                    }
                }
            }
        }

        return corners;
    }

    private static void sortCorners(List<Point> corners, Point center) {
        List<Point> top = new ArrayList<>(), bot = new ArrayList<>();
        for (int i = 0; i < corners.size(); i++) {
            if (corners.get(i).y < center.y)
                top.add(corners.get(i));
            else
                bot.add(corners.get(i));
        }

        Collections.sort(top);
        Collections.sort(bot);

        Point tl = top.get(0);
        Point tr = top.get(top.size() - 1);
        Point bl = bot.get(0);
        Point br = bot.get(bot.size() - 1);

        corners.clear();
        corners.add(tl);
        corners.add(tr);
        corners.add(br);
        corners.add(bl);
    }

}