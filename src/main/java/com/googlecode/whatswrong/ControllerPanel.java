package com.googlecode.whatswrong;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * ControllerPanel is a superclass for all controlling panels.
 *
 * @author Sebastian Riedel
 */
public abstract class ControllerPanel extends JPanel {


    /**
     * Creates new ControllerPanel.
     */
    public ControllerPanel() {
        setBorder(new EmptyBorder(5, 5, 5, 5));

    }
}
