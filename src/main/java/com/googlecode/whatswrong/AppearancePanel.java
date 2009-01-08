package com.googlecode.whatswrong;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An AppearancePanel controls the appearance of an NLPCanvas. This includes the configuration of the height and width
 * of the graph as well as the toggling of anti-aliasing and curved/rectangular modes.
 *
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MissingMethodJavaDoc"})
public class AppearancePanel extends JPanel implements NLPCanvas.Listener {
    private JSlider marginSlider;
    private JSlider heightSlider;
    private JCheckBox curved;
    private NLPCanvas nlpCanvas;
    private JCheckBox anti;

    /**
     * Creates a new AppearancePanel for the given canvas.
     *
     * @param nlpCanvas the NLPCanvas to control.
     */
    public AppearancePanel(final NLPCanvas nlpCanvas) {
        super(new GridBagLayout());
        this.nlpCanvas = nlpCanvas;
        nlpCanvas.addListener(this);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.weighty = 1.0;
        //c.fill = GridBagConstraints.HORIZONTAL;
        //setBorder(new TitledBorder(new EtchedBorder(),"Change Appearance"));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        marginSlider = new JSlider(JSlider.HORIZONTAL, 0, 100,
            nlpCanvas.getRenderer().getMargin());
        marginSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                nlpCanvas.getRenderer().setMargin(marginSlider.getValue());
                nlpCanvas.updateNLPGraphics();
            }
        });
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        add(new JLabel("Width:"), c);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 1;
        add(marginSlider, c);
        marginSlider.setToolTipText("Margin between tokens");
        marginSlider.setMaximumSize(new Dimension(20, 50));

        heightSlider = new JSlider(JSlider.HORIZONTAL, 10, 50,
            nlpCanvas.getRenderer().getHeightFactor());
        heightSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                nlpCanvas.getRenderer().setHeightFactor(heightSlider.getValue());
                nlpCanvas.updateNLPGraphics();
                //scrollToBottom();
            }
        });
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        add(new JLabel("Height:"), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        add(heightSlider, c);
        heightSlider.setToolTipText("Tree height");
        heightSlider.setMaximumSize(new Dimension(20, 50));

//    final JSlider widthSlider = new JSlider(JSlider.HORIZONTAL,10,100,nlpCanvas.getDependencyLayout().getHeightPerLevel());
//    widthSlider.addChangeListener(new ChangeListener() {
//      public void stateChanged(ChangeEvent e) {
//        nlpCanvas.getDependencyLayout().setVertexExtraSpace(widthSlider.getValue());
//        nlpCanvas.updateNLPGraphics();
//        //scrollToBottom();
//      }
//    });
//    c.gridx = 0;
//    c.anchor = GridBagConstraints.EAST;
//    add(new JLabel("Node:"),c);
//    c.gridx = 1;
//    c.anchor = GridBagConstraints.WEST;
//    add(widthSlider,c);
//    widthSlider.setToolTipText("Node width");
//    widthSlider.setMaximumSize(new Dimension(20,50));


        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 0;
        add(new JSeparator(), c);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        add(new JLabel("Edges:"), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        curved = new JCheckBox("Curved",
            nlpCanvas.getRenderer().isCurved());
        curved.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nlpCanvas.getRenderer().setCurved(curved.isSelected());
                nlpCanvas.updateNLPGraphics();
            }
        });
        add(curved, c);

        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        anti = new JCheckBox("Antialiasing",
            nlpCanvas.getRenderer().isAntiAliasing());
        anti.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nlpCanvas.getRenderer().setAntiAliasing(anti.isSelected());
                nlpCanvas.updateNLPGraphics();
            }
        });
        add(anti, c);

        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
//    final JCheckBox lines = new JCheckBox("Separation Lines", nlpCanvas.getSpanLayout().isSeparationLines());
//    lines.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        //nlpCanvas.getSpanLayout().setSeparationLines(lines.isSelected());
//        nlpCanvas.updateNLPGraphics();
//      }
//    });
//    add(lines, c);

        setMinimumSize(new Dimension(0, 150));


    }

    public void instanceChanged() {
        heightSlider.setValue(nlpCanvas.getRenderer().getHeightFactor());
        curved.setSelected(nlpCanvas.getRenderer().isCurved());
        marginSlider.setValue(nlpCanvas.getRenderer().getMargin());
        anti.setSelected(nlpCanvas.getRenderer().isAntiAliasing());
        //heightSlider.set
    }

    public void redrawn() {
    }
}
