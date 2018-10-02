package main;


import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.cvShowImage;
import static org.bytedeco.javacpp.opencv_highgui.cvWaitKey;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class GrayImage {
    public static void main(String args[]) {

        IplImage img = cvLoadImage("src/main/resources/bd/passport.png");

        cvResize(cvLoadImage("src/main/resources/bd/teste01.jpg"), img);

        IplImage grayImg = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 1);

        cvCvtColor(img, grayImg, CV_BGR2GRAY);

        cvShowImage("Original", img);
        cvShowImage("GRAY", grayImg);

        cvWaitKey();

        cvReleaseImage(img);
        cvReleaseImage(grayImg);

    }
}
