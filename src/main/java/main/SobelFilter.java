package main;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class SobelFilter {
    public static void main(String[] args) {

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
        imshow("Original image", src);
        imshow("Sobel Demo - Simple Edge Detector", sobel_img);
        waitKey(0);

    }
}