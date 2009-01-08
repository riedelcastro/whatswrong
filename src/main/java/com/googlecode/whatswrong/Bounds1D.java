package com.googlecode.whatswrong;

/**
 * @author Sebastian Riedel
 */
public class Bounds1D {

    public final int from, to;

    public Bounds1D(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getWidth() {
        return to - from;
    }

    public int getMiddle() {
        return from + getWidth() / 2;
    }
}
