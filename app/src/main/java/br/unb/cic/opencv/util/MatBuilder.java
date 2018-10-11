package br.unb.cic.opencv.util;

import org.opencv.core.Mat;

public class MatBuilder {
    private Mat mat;

    public MatBuilder(Mat mat){
        this.mat = mat;
    }

    public Mat getMat() {
        return mat;
    }

    public MatBuilder rgbToGray(){
        this.mat = ImageProcessing.convertToGray(this.mat);
        return this;
    }

    public MatBuilder sobel(){
        this.mat = ImageProcessing.sobelFilter(this.mat);
        return this;
    }

    public MatBuilder gaussian3(){
        this.mat = ImageProcessing.gaussian3(this.mat);
        return this;
    }
}