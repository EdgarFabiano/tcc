package br.unb.cic.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.convertScaleAbs;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.Sobel;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class SobelFilter {

    public void init() {
        Mat src, src_gray = new Mat();
        Mat sobel_img = new Mat();
        String imageName = "src/main/resources/bd/road_sign2.jpg";
        int scale = 1;
        int delta = 0;
        int ddepth = CV_16S;

        src = imread(imageName, IMREAD_COLOR); // Load an image

        if (src.empty()) {
            throw new RuntimeException("src empty");
        }

        GaussianBlur(src, src, new Size(3, 3), 0, 0, BORDER_DEFAULT);

        cvtColor(src, src_gray, COLOR_BGR2GRAY);

        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();

        Sobel(src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
        Sobel(src_gray, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);

        convertScaleAbs(grad_x, abs_grad_x);
        convertScaleAbs(grad_y, abs_grad_y);

        addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, sobel_img);

//        imshow("Original image", src);
//        imshow("Sobel Demo - Simple Edge Detector", sobel_img);
    }
}
