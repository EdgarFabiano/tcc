package main;

import org.bytedeco.javacpp.opencv_core.IplImage;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.cvShowImage;
import static org.bytedeco.javacpp.opencv_highgui.cvWaitKey;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;

public class Main {
    public static void main(String args[]){

        IplImage img = cvLoadImage("src/main/resources/bd/passport.png");

        IplImage hsvImg = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 3);
        IplImage grayImg = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 1);

        cvCvtColor(img, hsvImg, CV_BGR2HSV);
        cvCvtColor(img, grayImg, CV_BGR2GRAY);

        cvShowImage("Original", img);
        cvShowImage("HSV", hsvImg);
        cvShowImage("GRAY", grayImg);

        cvWaitKey();

        cvSaveImage("src/main/resources/bd/Original.jpg", img);
        cvSaveImage("src/main/resources/bd/HSV.jpg", hsvImg);
        cvSaveImage("src/main/resources/bd/GRAY.jpg", grayImg);

        cvReleaseImage(img);
        cvReleaseImage(hsvImg);
        cvReleaseImage(grayImg);

    }
}
