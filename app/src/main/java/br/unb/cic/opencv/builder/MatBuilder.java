package br.unb.cic.opencv.builder;

import org.opencv.core.Mat;

import br.unb.cic.opencv.util.ImageProcessing;

public class MatBuilder {
    private Mat mat;
    private Mat original;

    public MatBuilder(Mat mat){
        this.mat = mat;
        this.original = mat;
    }

    public Mat getMat() {
        return mat;
    }

    public Mat getOriginal() {
        return original;
    }

    public MatBuilder rgbToGray(){
        ImageProcessing.convertToGray(this.mat);
        return this;
    }

    public MatBuilder sobel(){
        this.mat = ImageProcessing.sobelFilter(this.mat);
        return this;
    }
    public MatBuilder canny(){
        this.mat = ImageProcessing.canny(this.mat);
        return this;
    }

    public MatBuilder gaussian3(){
        this.mat = ImageProcessing.gaussian3(this.mat);
        return this;
    }

    public MatBuilder resizeIfNecessary() {
        this.mat = ImageProcessing.resizeIfNecessary(this.mat);
        this.original = this.mat;
        return this;
    }

    public MatBuilder bestApproach() {
        this.mat = ImageProcessing.bestApproach(this.mat, original);
        return this;
    }

    public MatBuilder inpaint() {
        this.mat = ImageProcessing.inpaint(this.mat);
        return this;
    }
}