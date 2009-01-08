package com.googlecode.whatswrong;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A TokenFilterPanel controls a TokenFilter and updates a NLPCanvas whenever
 * the filter has been changed.
 *
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MissingMethodJavaDoc"})
public class TokenFilterPanel extends ControllerPanel implements ChangeListener {
  /**
   * The list model for the list of possible token properties.
   */
  private DefaultListModel listModel;
  /**
   * The JList of possible token properties.
   */
  private final JList list;
  /**
   * The canvas which is to be updated whenever the filter is changed.
   */
  private NLPCanvas canvas;

  /**
   * The filter this panel controls.
   */
  private TokenFilter tokenFilter;

  /**
   * Creates a new TokenFilterPanel for the given canvas and filter.
   *
   * @param canvas      the NLPCanvas to update whenever the filter is changed.
   * @param tokenFilter the TokenFilter to control by this panel.
   */
  public TokenFilterPanel(final NLPCanvas canvas,
                          final TokenFilter tokenFilter) {
    this.tokenFilter = tokenFilter;
    this.canvas = canvas;
    canvas.addChangeListenger(this);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JLabel label = new JLabel("Show Properties");
    label.setAlignmentX(CENTER_ALIGNMENT);
    add(label);
    listModel = new DefaultListModel();
    list = new JList(listModel);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    updateProperties();
    list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getFirstIndex() == -1 || list.getModel().getSize() == 0)
          return;
        for (int index = e.getFirstIndex(); index < e.getLastIndex() + 1; ++index) {
          if (list.isSelectedIndex(index)) {
            tokenFilter.removeForbiddenProperty(listModel.get(index).toString());
          } else {
            tokenFilter.addForbiddenProperty(listModel.get(index).toString());
          }
        }
        canvas.updateNLPGraphics();
      }

    });

    JScrollPane pane = new JScrollPane(list);
    add(pane);

    add(new JSeparator());
    JPanel allowedTokens = new JPanel(new GridBagLayout());
    allowedTokens.add(new JLabel("Token:"), new SimpleGridBagConstraints(0, true));
    final JTextField allowed = new FilterTextField("text1,text2,...");

    allowed.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent event) {
        tokenFilter.clearAllowedStrings();
        String[] split = allowed.getText().trim().split("[,]");
        for (String property : split)
          tokenFilter.addAllowedString(property);
        canvas.updateNLPGraphics();
      }
    });

    allowedTokens.add(allowed, new SimpleGridBagConstraints(0, false));
    allowedTokens.add(new JLabel("Options:"), new SimpleGridBagConstraints(1, true));
    final JCheckBox wholeWords = new JCheckBox("Whole Words");
    wholeWords.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        tokenFilter.setWholeWord(wholeWords.isSelected());
        canvas.updateNLPGraphics();
      }
    });
    allowedTokens.add(wholeWords, new SimpleGridBagConstraints(1, false));


    add(allowedTokens);
    pane.setPreferredSize(new Dimension(150, 100));
    pane.setMinimumSize(new Dimension(150, 100));


  }

  /**
   * Updates the list of available token properties.
   */
  private void updateProperties() {
    ArrayList<TokenProperty> sorted = new ArrayList<TokenProperty>(this.canvas.getUsedProperties());
    Collections.sort(sorted);
    int index = 0;
    listModel.clear();
    listModel = new DefaultListModel();
    DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
    for (TokenProperty p : sorted) {
      listModel.addElement(p);
      if (!this.tokenFilter.getForbiddenTokenProperties().contains(p)
        && !list.isSelectedIndex(index))
        selectionModel.addSelectionInterval(index, index);
      ++index;
    }
    list.setModel(listModel);
    list.setSelectionModel(selectionModel);

  }

  /**
   * Updates available properties and requests a redraw of the panel.
   *
   * @param e the ChangeEvent corresponding to the change of the canvas.
   */
  public void stateChanged(final ChangeEvent e) {
    updateProperties();
    repaint();


  }
}
