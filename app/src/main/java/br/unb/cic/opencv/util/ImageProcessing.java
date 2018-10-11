package br.unb.cic.opencv.util;

import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.convertScaleAbs;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.Sobel;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class ImageProcessing {

    private ImageProcessing() {
        throw new UnsupportedOperationException("No " + ImageProcessing.class.getSimpleName() + " instances for you!");
    }

    public static void checkOpenCV() {
        Log.d("OpenCV_LIB",
                OpenCVLoader.initDebug() ?
                        "OpenCV successfully Loaded" : "OpenCV not Loaded");
    }

    public static Mat gaussian3(Mat mat){
        Mat dst = new Mat();
        GaussianBlur(mat, dst, new Size(3, 3), 0, 0, BORDER_DEFAULT);
        return dst;
    }

    public static Mat gaussian(Mat mat, double size){
        Mat dst = new Mat();
        GaussianBlur(mat, dst, new Size(size, size), 0, 0, BORDER_DEFAULT);
        return dst;
    }

    public static Mat convertToGray(Mat mat) {
        Mat dst = new Mat();
        cvtColor(mat, dst, COLOR_BGR2GRAY);
        return dst;
    }

    public static Mat sobelFilter(Mat mat) {
        Mat aux = new Mat();
        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();

        int ddepth = CV_16S, scale = 1, delta = 0;
        Sobel(mat, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
        Sobel(mat, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);

        convertScaleAbs(grad_x, abs_grad_x);
        convertScaleAbs(grad_y, abs_grad_y);

        addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, aux);

        return aux;
    }

}
