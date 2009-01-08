package com.googlecode.whatswrong;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * An EdgeTypeFilterPanel controls an EdgeTypeFilter and requests an update for
 * an NLPCanvas whenever the filter is changed.
 *
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MissingMethodJavaDoc"})
public class EdgeTypeFilterPanel extends ControllerPanel
  implements NLPCanvas.Listener, EdgeTypeFilter.Listener {

  /**
   * The canvas to request the update after the filter has been changed.
   */
  private NLPCanvas nlpCanvas;
  /**
   * The swing list of available edge types.
   */
  private JList types;
  /**
   * The backing model for the swing list of edge types.
   */
  private DefaultListModel listModel;
  /**
   * The checkbox for showing matches,
   */
  private JCheckBox matches;
  /**
   * The checkbox for showing False Positives.
   */
  private JCheckBox falsePositives;
  /**
   * The checkbox for showing False Negatives.
   */
  private JCheckBox falseNegatives;

  /**
   * The set of types for which the state (filtered/not filtered) has just been
   * changed through this controller.
   */
  private HashSet<String> justChanged = new HashSet<String>();

  /**
   * The filter that this panel changes.
   */
  private EdgeTypeFilter edgeTypeFilter;


  /**
   * Creates a new EdgeTypeFilterPanel for the given canvas and filter.
   *
   * @param nlpCanvas      the canvas that should be updated when the filter is
   *                       changed.
   * @param edgeTypeFilter the filter that should be controlled by this panel.
   */
  public EdgeTypeFilterPanel(final NLPCanvas nlpCanvas,
                             final EdgeTypeFilter edgeTypeFilter) {
    this.edgeTypeFilter = edgeTypeFilter;
    setLayout(new GridBagLayout());
    nlpCanvas.addListener(this);
    edgeTypeFilter.addListener(this);
    //setLayout(new GridBagLayout());
    //setBorder(new TitledBorder(new EtchedBorder(), title));
    this.nlpCanvas = nlpCanvas;
    //setPreferredSize(new Dimension(200,80));
    listModel = new DefaultListModel();
    //listModel.addElement("Blah");
    types = new JList(listModel);
    types.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


    matches = new JCheckBox("Matches");
    falsePositives = new JCheckBox("False Positives");
    falseNegatives = new JCheckBox("False Negatives");

    updateTypesList();
    updateSelection();
    types.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        justChanged.clear();
        if (e.getFirstIndex() == -1 || e.getLastIndex() >= types.getModel().getSize())
          return;
        for (int index = e.getFirstIndex(); index < e.getLastIndex() + 1; ++index) {
          String type = types.getModel().getElementAt(index).toString();
          justChanged.add(type);
          if (types.isSelectedIndex(index)) {
            edgeTypeFilter.addAllowedPrefixType(type);
          } else {
            edgeTypeFilter.removeAllowedPrefixType(type);
          }
        }
        justChanged.clear();
        nlpCanvas.updateNLPGraphics();

      }
    });
    JScrollPane pane = new JScrollPane(types);
    add(pane, new SimpleGridBagConstraints(0, 0, 2, 2));
    pane.setPreferredSize(new Dimension(150, 70));
    //pane.setMinimumSize(new Dimension(pane.getMinimumSize().width, 100));

    //add false positive/negative and match check buttons

    matches.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (matches.isSelected())
          edgeTypeFilter.addAllowedPostfixType("Match");
        else {
          edgeTypeFilter.removeAllowedPostfixType("Match");
        }
        justChanged.clear();
        nlpCanvas.updateNLPGraphics();
      }
    });
    falseNegatives.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (falseNegatives.isSelected())
          edgeTypeFilter.addAllowedPostfixType("FN");
        else
          edgeTypeFilter.removeAllowedPostfixType("FN");

        nlpCanvas.updateNLPGraphics();
      }
    });
    falsePositives.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (falsePositives.isSelected())
          edgeTypeFilter.addAllowedPostfixType("FP");
        else
          edgeTypeFilter.removeAllowedPostfixType("FP");

        nlpCanvas.updateNLPGraphics();
      }
    });

    add(matches, new SimpleGridBagConstraints(0, 2, 2, 1));
    add(falsePositives, new SimpleGridBagConstraints(0, 3, 2, 1));
    add(falseNegatives, new SimpleGridBagConstraints(0, 4, 2, 1));

    //updateTypesList();
  }

  /**
   * Separates the types in <code>usedTypes</code> into prefix and postfix
   * types.
   *
   * @param usedTypes    the types to separate.
   * @param prefixTypes  the target set for prefix types.
   * @param postfixTypes the target set for postfix types.
   */
  private void separateTypes(final Set<String> usedTypes,
                             final HashSet<String> prefixTypes,
                             final HashSet<String> postfixTypes) {
    for (String type : usedTypes) {
      int index = type.indexOf(':');
      if (index == -1)
        prefixTypes.add(type);
      else {
        prefixTypes.add(type.substring(0, index));
        postfixTypes.add(type.substring(index + 1));

      }
    }
  }


  /**
   * Updates the set of selected (set to be visible) edge types.
   */
  private void updateSelection() {
    ListSelectionModel selectionModel = types.getSelectionModel();
    selectionModel.setValueIsAdjusting(true);
    for (int index = 0; index < types.getModel().getSize(); ++index) {
      String type = types.getModel().getElementAt(index).toString();
      if (edgeTypeFilter.allowsPrefix(type))
        selectionModel.addSelectionInterval(index, index);
    }

    selectionModel.setValueIsAdjusting(false);
  }


  /**
   * Updates the list of available edge types and the set FP/FN/Match
   * checkboxes.
   */
  private void updateTypesList() {

    HashSet<String> prefixTypes = new HashSet<String>();
    HashSet<String> postfixTypes = new HashSet<String>();
    separateTypes(nlpCanvas.getUsedTypes(), prefixTypes, postfixTypes);
    ArrayList<String> allTypes = new ArrayList<String>();
    allTypes.addAll(prefixTypes);

    falsePositives.setEnabled(postfixTypes.contains("FP"));
    falsePositives.setSelected(edgeTypeFilter.allowsPostfix("FP"));
    falseNegatives.setEnabled(postfixTypes.contains("FN"));
    falseNegatives.setSelected(edgeTypeFilter.allowsPostfix("FN"));
    matches.setEnabled(postfixTypes.contains("Match"));
    matches.setSelected(edgeTypeFilter.allowsPostfix("Match"));

    listModel.clear();
    for (String type : allTypes) {
      listModel.addElement(type);
    }

  }


  /**
   * Updates the type list and the selection. Afterwards request for repaint is
   * issued.
   */
  public void instanceChanged() {
    updateTypesList();
    updateSelection();
    repaint();
  }

  /**
   * Called whenever the {@link NLPCanvas#updateNLPGraphics()} method is called.
   * Does nothing.
   *
   * @see com.googlecode.whatswrong.NLPCanvas.Listener#redrawn()
   */
  public void redrawn() {

  }

  /**
   * Updates the selection.
   *
   * @param type type string that was allowed or disallowed.
   * @see com.googlecode.whatswrong.EdgeTypeFilter.Listener#changed(String)
   */
  public void changed(String type) {
    if (!justChanged.contains(type)) {
      updateSelection();
      //repaint();
    }
  }
}
