package br.unb.cic.opencv.util;

import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.opencv.calib3d.Calib3d.findHomography;
import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.split;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2RGB;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.LINE_AA;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.arcLength;
import static org.opencv.imgproc.Imgproc.bilateralFilter;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.morphologyEx;
import static org.opencv.imgproc.Imgproc.threshold;
import static org.opencv.imgproc.Imgproc.warpPerspective;

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
        Imgproc.HoughLinesP(mat, lines, 1, 2 * Math.PI / 180, 50, minImageDimention / 20, 50);

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

        System.out.println("ANTES: " + imageLines.size());
        imageLines = processa(imageLines);
        System.out.println("DEPOIS: " + imageLines.size());

        for (Line line : imageLines) {
            Imgproc.arrowedLine(mat, line.start, line.end, color, thickness);
        }
        // TODO: pre processar a imagem para imendar linhas pequenas e sequenciais em uma só
        // Estrutura de dados para quadrados. Detectá-los um por um

//        for (int i = 0; i < imageLines.size(); i++) {
//            Line lineI = imageLines.get(i);
//
//            for (int j = 0; j < imageLines.size(); j++) {
//                Line lineJ = imageLines.get(j);
//                if (!lineI.equals(lineJ)) {
//                    boolean isAngle90 = Math.abs(lineI.angleBetween(lineJ)) < Math.PI / 2 + toleranceFactor && Math.abs(lineI.angleBetween(lineJ)) > Math.PI / 2 - toleranceFactor;
//                    boolean isAngleEqual = Math.abs(lineI.angleBetween(lineJ)) < toleranceFactor && Math.abs(lineI.angleBetween(lineJ)) > -toleranceFactor;
//
//                    if (lineI.isNeighbour(lineJ) && isAngle90) {
//                        Imgproc.arrowedLine(mat, lineI.start, lineI.end, color, thickness);
//                        imageLines.remove(lineI);
//                    } else if ((lineI.distanceBetween(lineJ) > minImageDimention / 5 && isAngleEqual)) {
//                        Imgproc.arrowedLine(mat, lineI.start, lineI.end, color, thickness);
//                        imageLines.remove(lineI);
//                    }
//                }
//            }
//
//        }

        return mat;
    }

    private static List<Line> processa(List<Line> imageLines) {
        List<Line> aux = new ArrayList<>();
        double toleranceFactor = Math.PI / 36;
        for (int i = 0; i < imageLines.size(); i++) {
            Line lineI = imageLines.get(i);

            for (int j = i; j < imageLines.size(); j++) {
                Line lineJ = imageLines.get(j);
                //TODO diminuir fator de tolerancia
                boolean isAngleEqual = Math.abs(lineI.angleBetween(lineJ)) < toleranceFactor && Math.abs(lineI.angleBetween(lineJ)) > -toleranceFactor;

                //TODO considerar a orientação cartesiana do OpenCV
                if (isAngleEqual && lineI.isNeighbour(lineJ)) {
                    Point newStart, newEnd;
                    if (lineI.isHorizontal()) {
                        if (Math.min(lineI.start.x, lineJ.start.x) == lineI.start.x) {
                            newStart = lineI.start;
                        } else {
                            newStart = lineJ.start;
                        }

                        if (Math.max(lineI.end.x, lineJ.end.x) == lineI.end.x) {
                            newEnd = lineI.end;
                        } else {
                            newEnd = lineJ.end;
                        }

                    } else {
                        if (Math.min(lineI.start.y, lineJ.start.y) == lineI.start.y) {
                            newStart = lineI.start;
                        } else {
                            newStart = lineJ.start;
                        }

                        if (Math.max(lineI.end.y, lineJ.end.y) == lineI.end.y) {
                            newEnd = lineI.end;
                        } else {
                            newEnd = lineJ.end;
                        }
                    }

                    aux.add(i, new Line(newStart, newEnd));
                    if (i != 0) {
                        i--;
                    }
                    imageLines.remove(lineI);
                    imageLines.remove(lineJ);
                }
            }
        }
        return aux;
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

    public static Mat secondApproach(Mat mat) {

        double DELAY = 0.02;
        int USE_CAM = 1;
        int IS_FOUND = 0;
        int MORPH = 7;
        int CANNY = 250;
        double _width = 600.0;
        double _height = 420.0;
        double _margin = 0.0;

        cvtColor(mat, mat, COLOR_RGBA2RGB);
        Mat gray = mat.clone();
        cvtColor(mat, gray, COLOR_BGRA2GRAY);

        bilateralFilter(mat, gray, 1, 10, 120);

        Mat edges = new Mat();
        Canny(gray, edges, 10, CANNY);

        Mat kernel = new Mat();
        getStructuringElement(MORPH_RECT, new Size(MORPH, MORPH));

        Mat closed = new Mat();
        morphologyEx(edges, closed, MORPH_CLOSE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat h = new Mat(); //hierarchy
        findContours(closed, contours, h, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);// detecta bordas hor, vert e diag

        Mat out = mat.clone();

        for (MatOfPoint cont : contours) {

            if (contourArea(cont) > 5) {
                double arc_len = arcLength(new MatOfPoint2f(cont.toArray()), true);
                MatOfPoint2f approx = new MatOfPoint2f();
                approxPolyDP(new MatOfPoint2f(cont.toArray()), approx, 0.1 * arc_len, true);

                if (approx.size() == new Size(4, 4)) {
                    IS_FOUND = 1;

                    MatOfPoint2f pts_src = new MatOfPoint2f(approx);
                    pts_src.convertTo(pts_src, CV_32FC2);

                    MatOfPoint2f pts_dst = new MatOfPoint2f();
                    pts_src.convertTo(pts_dst, CV_32FC2);
                    Mat status = findHomography(pts_src, pts_dst);
                    status.convertTo(status, CV_32FC2);

                    cvtColor(out, out, COLOR_RGBA2RGB);
//                    mat.convertTo(mat, CV_32FC2);
                    warpPerspective(mat, out, status, new Size((int) (_width + _margin * 2), (int) (_height + _margin * 2)));
                    drawContours(mat, contours, -1, new Scalar(255, 0, 0), 2);
                }
            }

        }


        return closed;
    }

    public static Mat thirdApproach(Mat src) {
        Random rng = new Random(12345);
        int threshold = 100;
        Mat srcGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(srcGray, srcGray, new Size(3, 3));
        Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Imgproc.drawContours(drawing, contours, i, color, 2, Core.LINE_8, hierarchy, 0, new Point());
        }

        return drawing;
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

}