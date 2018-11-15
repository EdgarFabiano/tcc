package br.unb.cic.opencv.util;

import org.opencv.core.Point;

import java.util.Objects;

public class Line {

    public Point start, end;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public double size() {
        return Math.sqrt((end.y - start.y) * (end.y - start.y) + (end.x - start.x) * (end.x - start.x));
    }

    public Point at(double x) {
        Point point = new Point();
        double m = (end.y - start.y) / (end.x - start.x);
        point.x = x;
        point.y = m * (end.x - start.x) + start.y;
        return point;
    }

    public double angleBetween(Line line) {
        double m1 = end.x - start.x == 0 ? Math.PI/2 : (end.y - start.y) / (end.x - start.x);
        double m2 = line.end.x - line.start.x == 0 ? Math.PI/2 : (line.end.y - line.start.y) / (line.end.x - line.start.x);
        return Math.atan((m2 - m1) / (1 + (m1 * m2)));
    }

    public double distanceBetween(Line line) {
        // Lida com o caso de interseção
        if (((line.start.x <= this.end.x && line.start.x >= this.start.x) ||
                (line.end.x <= this.end.x && line.end.x >= this.start.x)) &&
                ((line.start.y <= this.end.y && line.start.y >= this.start.y) ||
                        (line.end.y <= this.end.y && line.end.y >= this.start.y))) {
            return 0;
        }
        double min1 = Math.min(distanceBetween(line.start, this.start), distanceBetween(line.end, this.start));
        double min2 = Math.min(distanceBetween(line.start, this.end), distanceBetween(line.end, this.end));
        return Math.min(min1, min2);
    }

    private double distanceBetween(Point a, Point b) {
        return Math.sqrt((a.y - b.y) * (a.y - b.y) + (a.x - b.x) * (a.x - b.x));
    }

    public boolean isNeighbour(Line line) {
        return distanceBetween(line) <= 5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return (this.start.x == line.start.x) && (this.end.x == line.end.x) && (this.start.y == line.start.y) && (this.end.y == line.end.y);
    }

}
