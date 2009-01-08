package com.googlecode.whatswrong;

import java.awt.*;

/**
 * A SimpleGridBagConstraints object is a GridBagConstraints object that can be created using a set of convenience
 * constructors. This allows us to create constraints more easily and makes GridBagConstraint-based code more readable.
 *
 * @author Sebastian Riedel
 */
public class SimpleGridBagConstraints extends GridBagConstraints {

    /**
     * Creates a SimpleGridBagConstraints object with default properties and the given gridx and gridy attributes.
     *
     * @param gridx the x-position in the grid
     * @param gridy the y-position in the grid
     */
    public SimpleGridBagConstraints(final int gridx, final int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
    }


    public SimpleGridBagConstraints(int gridx, int gridy, double weightx, double weighty, int anchor, int fill) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.weightx = weightx;
        this.weighty = weighty;
        this.anchor = anchor;
        this.fill = fill;
    }

    public SimpleGridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.weightx = gridwidth > 1 ? 1.0 : 0;
        this.weighty = gridheight > 1 ? 1.0 : 0;
        this.gridwidth = gridwidth;
        this.gridheight = gridheight;
        this.fill = gridwidth > 1 ? (gridheight > 1 ? BOTH : HORIZONTAL) : (gridheight > 1 ? VERTICAL : NONE);
    }


    public SimpleGridBagConstraints(int gridy, boolean left) {
        this(gridy, left, !left);

    }

    public SimpleGridBagConstraints(int gridy, boolean left, boolean fill) {
        this.gridy = gridy;
        this.gridx = left ? 0 : 1;
        this.weightx = left ? 0.0 : 1.0;
        this.weighty = 0.0;
        this.fill = fill ? HORIZONTAL : NONE;
        this.anchor = left ? EAST : WEST;
    }

}
