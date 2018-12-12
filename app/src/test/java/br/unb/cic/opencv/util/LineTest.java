package br.unb.cic.opencv.util;

import org.junit.Test;
import org.opencv.core.Point;

import static org.junit.Assert.*;

public class LineTest {

    @Test
    public void name() {
        Line lineA = new Line(new Point(2,1), new Point(6,5));
        Line lineB = new Line(new Point(3,0), new Point(1,3));

        System.out.println(lineA.distanceBetween(lineB));
    }

    @Test
    public void testSize() {
        Line line = new Line(new Point(0, 0), new Point(3,4));
        assertEquals(5L, line.size(), 0);
    }

    @Test
    public void testAngleBetween() {
        Line lineA = new Line(new Point(1, 2), new Point(1,5));
        Line lineB = new Line(new Point(1, 2), new Point(2,2));
        System.out.println(lineA.distanceBetween(lineB));
        System.out.println(lineB.distanceBetween(lineA));
        System.out.println(lineA.distanceBetween(lineA));
        System.out.println();
        System.out.println(lineA.angleDegBetween(lineB));

    }
}