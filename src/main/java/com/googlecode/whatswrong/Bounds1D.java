package com.googlecode.whatswrong;

/**
 * This class represents one dimensional bounds.
 *
 * @author Sebastian Riedel
 */
public class Bounds1D {

    /**
     * Where do the bounds start
     */
    public final int from;

    /**
     * Where do the bounds end.
     */
    public final int to;

    /**
     * Create a new Bounds1D object for the given bounds.
     *
     * @param from where do the bounds start.
     * @param to   where do the bounds end.
     */
    public Bounds1D(int from, int to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Return the total width of the bounds
     *
     * @return width of bounds.
     */
    public int getWidth() {
        return to - from;
    }

    /**
     * Get middle of the bounds.
     *
     * @return the middle of the bounds.
     */
    public int getMiddle() {
        return from + getWidth() / 2;
    }
}
