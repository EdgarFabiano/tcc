package br.unb.cic.opencv.util;

import org.opencv.core.Point;

import java.io.Serializable;

class Square implements Serializable {

    public Line left, right, top, bottom;
    public Point tl, tr, bl, br;

    public Square(Point tl, Point tr, Point bl, Point br) {
        this.tl = tl;
        this.tr = tr;
        this.bl = bl;
        this.br = br;

        this.left = new Line(bl, tl);
        this.right = new Line(br, tr);
        this.top = new Line(tl, tr);
        this.bottom = new Line(bl, br);
    }

    public double area() {
        double max1 = Math.max(this.bottom.size() * this.left.size(), this.bottom.size() * this.right.size());
        double max2 = Math.max(this.top.size() * this.left.size(), this.top.size() * this.right.size());
        return Math.max(max1, max2);
    }

}
