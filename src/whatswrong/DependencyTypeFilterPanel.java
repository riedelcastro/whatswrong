package whatswrong;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sebastian Riedel
 */
public class DependencyTypeFilterPanel extends ControllerPanel
  implements NLPCanvas.Listener, EdgeTypeFilter.Listener {
  private NLPCanvas nlpCanvas;
  private String title;
  private JList types;
  private DefaultListModel listModel;
  private JCheckBox matches;
  private JCheckBox falsePositives;
  private JCheckBox falseNegatives;
  private HashSet<String> justChanged = new HashSet<String>();


  public DependencyTypeFilterPanel(String title, final NLPCanvas nlpCanvas) {
    setLayout(new GridBagLayout());
    nlpCanvas.addListener(this);
    nlpCanvas.getDependencyTypeFilter().addListener(this);
    //setLayout(new GridBagLayout());
    //setBorder(new TitledBorder(new EtchedBorder(), title));
    this.title = title;
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
        EdgeTypeFilter filter = nlpCanvas.getDependencyTypeFilter();
        justChanged.clear();
        if (e.getFirstIndex() == -1 || e.getLastIndex() >= types.getModel().getSize())
          return;
        for (int index = e.getFirstIndex(); index < e.getLastIndex() + 1; ++index) {
          String type = types.getModel().getElementAt(index).toString();
          justChanged.add(type);
          if (types.isSelectedIndex(index)) {
            filter.addAllowedPrefixType(type);
          } else {
            filter.removeAllowedPrefixType(type);
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
        EdgeTypeFilter filter = nlpCanvas.getDependencyTypeFilter();
        if (matches.isSelected())
          filter.addAllowedPostfixType("Match");
        else {
          filter.removeAllowedPostfixType("Match");
        }
        justChanged.clear();
        nlpCanvas.updateNLPGraphics();
      }
    });
    falseNegatives.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        EdgeTypeFilter filter = nlpCanvas.getDependencyTypeFilter();
        if (falseNegatives.isSelected())
          filter.addAllowedPostfixType("FN");
        else
          filter.removeAllowedPostfixType("FN");

        nlpCanvas.updateNLPGraphics();
      }
    });
    falsePositives.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        EdgeTypeFilter filter = nlpCanvas.getDependencyTypeFilter();
        if (falsePositives.isSelected())
          filter.addAllowedPostfixType("FP");
        else
          filter.removeAllowedPostfixType("FP");

        nlpCanvas.updateNLPGraphics();
      }
    });

    add(matches, new SimpleGridBagConstraints(0, 2, 2, 1));
    add(falsePositives, new SimpleGridBagConstraints(0, 3, 2, 1));
    add(falseNegatives, new SimpleGridBagConstraints(0, 4, 2, 1));

    //updateTypesList();
  }

  private void separateTypes(Set<String> usedTypes, HashSet<String> prefixTypes, HashSet<String> postfixTypes) {
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


  public String getTitle() {
    return title;
  }


  public void stateChanged(ChangeEvent e) {
    //updateTypesList();
    //repaint();
  }

  private void updateSelection() {
    ListSelectionModel selectionModel = types.getSelectionModel();
    selectionModel.setValueIsAdjusting(true);
    for (int index = 0;  index < types.getModel().getSize();++index) {
      String type = types.getModel().getElementAt(index).toString();
      if (nlpCanvas.getDependencyTypeFilter().allowsPrefix(type))
        selectionModel.addSelectionInterval(index, index);
    }

    selectionModel.setValueIsAdjusting(false);
  }


  private void updateTypesList() {

    HashSet<String> prefixTypes = new HashSet<String>();
    HashSet<String> postfixTypes = new HashSet<String>();
    separateTypes(nlpCanvas.getUsedTypes(), prefixTypes, postfixTypes);
    ArrayList<String> allTypes = new ArrayList<String>();
    allTypes.addAll(prefixTypes);

    falsePositives.setEnabled(postfixTypes.contains("FP"));
    falsePositives.setSelected(nlpCanvas.getDependencyTypeFilter().allowsPostfix("FP"));
    falseNegatives.setEnabled(postfixTypes.contains("FN"));
    falseNegatives.setSelected(nlpCanvas.getDependencyTypeFilter().allowsPostfix("FN"));
    matches.setEnabled(postfixTypes.contains("Match"));
    matches.setSelected(nlpCanvas.getDependencyTypeFilter().allowsPostfix("Match"));

    listModel.clear();
    for (String type : allTypes) {
      listModel.addElement(type);
    }

  }


  public void instanceChanged() {
    updateTypesList();
    updateSelection();
    repaint();
  }

  public void redrawn() {

  }

  public void changed(String type) {
    if (!justChanged.contains(type)) {
      updateSelection();
      //repaint();
    }
  }
}
