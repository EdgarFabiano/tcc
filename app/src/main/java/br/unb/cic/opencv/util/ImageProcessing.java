package br.unb.cic.opencv.util;

import android.support.annotation.NonNull;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.Mat.zeros;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.HoughLinesP;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.floodFill;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.threshold;

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
        GaussianBlur(mat, dst, new Size(9, 9), 0, 0, BORDER_DEFAULT);
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
    public static void convertToGray(Mat mat) {
        cvtColor(mat, mat, COLOR_BGR2GRAY);
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

    private static void dilateErode(Mat binaryImg) {
        Mat kernel10 = getStructuringElement(MORPH_RECT, new Size(10, 10));
        Mat kernel20 = getStructuringElement(MORPH_RECT, new Size(20, 20));

        dilate(binaryImg, binaryImg, kernel20);
        erode(binaryImg, binaryImg, kernel10);
    }

    private static void binarize(Mat src) {
        int CV_THRESH_BINARY = 0;
        if (src.type() != CvType.CV_8UC1) {
//            src.create(src.rows(), src.cols(), CvType.CV_8UC1);
            src.convertTo(src, CvType.CV_8UC1);
            src.reshape(1);
        }
        adaptiveThreshold(src, src, 255, ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 85, 10);
        bitwise_not(src, src);
    }

    public static Mat bestApproach(Mat src, Mat original) {
        int minImageDimention = Math.min(src.width(), src.height());
        int maxImageDimention = Math.max(src.width(), src.height());
        Mat out = original.clone();

        binarize(src);

        dilateErode(src);

        Mat kernel20 = getStructuringElement(MORPH_RECT, new Size(maxImageDimention/100, maxImageDimention/100));
        erode(src, src, kernel20);

        Mat lines = new Mat();
        HoughLinesP(src, lines, 2, 2 * Math.PI / 180, 50, minImageDimention / 2D, minImageDimention / 10D);

        List<Line> imageLines = new ArrayList<>();

        for (int i = lines.rows() - 1; i >= 0; i--) {
            double[] vec = lines.get(i, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Line line = new Line(start, end);
            imageLines.add(line);
//            Imgproc.line(out, start, end, new Scalar(255, 0, 0), 3);

        }

        List<List<Point>> corners = computeLines(imageLines, src);
        List<Square> squares = new ArrayList<>();

        for (int i = 0; i < corners.size(); i++) {
            Point center = new Point(0, 0);
            if (corners.get(i).size() < 4) continue;
            for (int j = 0; j < corners.get(i).size(); j++) {
                center.x += corners.get(i).get(j).x;
                center.y += corners.get(i).get(j).y;
            }
            center.x *= (1. / corners.get(i).size());
            center.y *= (1. / corners.get(i).size());
            squares.add(sortCorners(corners.get(i), center));
        }

        squares.sort(Comparator.comparingDouble(Square::area).reversed());
        Optional<Square> first = squares.stream().findFirst();
        if (first.isPresent()) {
            Square square = first.get();
//            drawLine(out, square);
            warpPerspective(out, square);

        }

        return out;
    }

    private static void warpPerspective(Mat inputMat, Square square) {

        List<Point> source = new ArrayList<>();
        source.add(square.br);
        source.add(square.bl);
        source.add(square.tl);
        source.add(square.tr);
        Mat startM = Converters.vector_Point2f_to_Mat(source);

        Rect r = boundingRect(new MatOfPoint(square.bl, square.br, square.tl, square.tr));

        int resultWidth = r.width;
        int resultHeight = r.height;

        Point ocvPOut4 = new Point(0, 0);
        Point ocvPOut1 = new Point(0, resultHeight);
        Point ocvPOut2 = new Point(resultWidth, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, 0);

        if (inputMat.height() > inputMat.width()) {

            ocvPOut3 = new Point(0, 0);
            ocvPOut4 = new Point(0, resultHeight);
            ocvPOut1 = new Point(resultWidth, resultHeight);
            ocvPOut2 = new Point(resultWidth, 0);
        }

        List<Point> dest = new ArrayList<>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);

        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, inputMat, perspectiveTransform, new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);

    }

    private static Point computeIntersection(Line l1, Line l2) {
        double x1 = l1.end.x, x2 = l1.start.x, y1 = l1.end.y, y2 = l1.start.y;
        double x3 = l2.end.x, x4 = l2.start.x, y3 = l2.end.y, y4 = l2.start.y;
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (d < 0) {
            Point pt = new Point();
            pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

            int threshold = 5;
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
     * Se ambas as linhas estiverem indefinidas, crie um novo grupo delas.
     * Se apenas uma linha for definida em um grupo, adicione a outra linha ao grupo.
     * Se ambas as linhas estiverem definidas, adicione todas as linhas de um grupo ao outro grupo.
     * Se ambas as linhas estiverem no mesmo grupo, não faça nada
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

    private static Square sortCorners(List<Point> corners, Point center) {
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
        return new Square(tl, tr, bl, br);
    }

    private static void drawLine(Mat out, Square square) {
        Imgproc.line(out, square.tl, square.tr, new Scalar(255, 0, 0), 3);
        Imgproc.line(out, square.tl, square.bl, new Scalar(255, 0, 0), 3);
        Imgproc.line(out, square.bl, square.br, new Scalar(255, 0, 0), 3);
        Imgproc.line(out, square.br, square.tr, new Scalar(255, 0, 0), 3);
    }

    public static Mat inpaint(Mat rgba) {

        Mat mask = getMaskInRange(rgba);

        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGBA2RGB);

        Mat out = new Mat();
        Photo.inpaint(rgba, mask, out, 20D, Photo.INPAINT_TELEA);

        return out;
    }

    @NonNull
    private static Mat getMaskInRange(Mat rgba) {
        Mat mHSV = new Mat();
        Imgproc.cvtColor(rgba, mHSV, Imgproc.COLOR_BGR2HSV);
        Mat mask = new Mat();
        Core.inRange(mHSV, new Scalar(16, 19, 27), new Scalar(254, 254, 254), mask);
        closeHoles(mask);
        invertIfNecessary(mask);
        fillHoles(mask);
        return mask;
    }

    @NonNull
    private static Mat getMaskKmeans(Mat rgba) {
        Mat mask = Cluster.cluster(rgba, 2).get(0);
        convertToGray(mask);
        binarize(mask);
        closeHoles(mask);
        invertIfNecessary(mask);
        fillHoles(mask);
        return mask;
    }

    private static void fillHoles(Mat mask) {
        Mat holes = mask.clone();
        Mat aux = zeros(new Size(mask.cols() + 2, mask.rows() + 2), CvType.CV_8UC1);
        floodFill(holes, aux, new Point(0, 0), new Scalar(255));
        Core.bitwise_not(holes, holes);
        Core.bitwise_or(mask, holes, mask);
    }

    private static void closeHoles(Mat mask) {
        int maxImageDimention = Math.max(mask.width(), mask.height());
        Mat kernel = getStructuringElement(MORPH_RECT, new Size(maxImageDimention/100, maxImageDimention/100));
        Imgproc.erode(mask, mask, kernel);
        Mat kernel2 = getStructuringElement(MORPH_RECT, new Size(maxImageDimention/150, maxImageDimention/150));
        Imgproc.dilate(mask, mask, kernel2);
        Mat kernel3 = getStructuringElement(MORPH_RECT, new Size(maxImageDimention/30, maxImageDimention/30));
        Imgproc.dilate(mask, mask, kernel3);
    }

    private static void invertIfNecessary(Mat mask) {
        if (getBlackProportion(mask) < 0.5) {
            Core.bitwise_not(mask, mask);
        }
    }

    private static double getBlackProportion(Mat img) {
        int imgSize = img.rows() * img.cols();
        int nonzero = Core.countNonZero(img);


        return (imgSize - nonzero) / (double) (imgSize);
    }

    public static Mat enhance(Mat mat) {
        cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB);
        for (int i = 0; i < mat.cols(); i++) {
            for (int j = 0; j < mat.rows(); j++) {
                double[] values = mat.get(j, i);
                values[0] = values[0] > 180 ? 255 : values[0];
                values[1] = values[1] > 180 ? 255 : values[1];
                values[2] = values[2] > 180 ? 255 : values[2];
                mat.put(j, i, values);
            }
        }

        return mat;
    }
}