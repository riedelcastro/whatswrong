package whatswrong;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

/**
 * @author Sebastian Riedel
 */
public class DependencyTypeFilterPanel extends ControllerPanel
  implements NLPCanvas.Listener, DependencyTypeFilter.Listener {
  private NLPCanvas nlpCanvas;
  private String title;
  private JList types;
  private DefaultListModel listModel;


  public DependencyTypeFilterPanel(String title, final NLPCanvas nlpCanvas) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    nlpCanvas.addListener(this);
    //setLayout(new GridBagLayout());
    //setBorder(new TitledBorder(new EtchedBorder(), title));
    this.title = title;
    this.nlpCanvas = nlpCanvas;
    //setPreferredSize(new Dimension(200,80));
    listModel = new DefaultListModel();
    //listModel.addElement("Blah");
    types = new JList(listModel);
    types.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    updateTypesList();    
    types.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        DependencyTypeFilter filter = nlpCanvas.getDependencyTypeFilter();
        if (e.getFirstIndex() == -1 || e.getLastIndex() >= types.getModel().getSize())
          return;
        for (int index = e.getFirstIndex(); index < e.getLastIndex() + 1; ++index) {
          if (types.isSelectedIndex(index)) {
            filter.addAllowedType(types.getModel().getElementAt(index).toString());
          } else {
            filter.removeAllowedType(types.getModel().getElementAt(index).toString());
          }
        }
        nlpCanvas.updateNLPGraphics();

      }
    });
    //types.
    JScrollPane pane = new JScrollPane(types);
    add(pane);
    pane.setPreferredSize(new Dimension(150, 100));
    //pane.setMinimumSize(new Dimension(pane.getMinimumSize().width, 100));

    updateTypesList();
  }

  private void separateTypes(Set<String> usedTypes, HashSet<String> prefixTypes, HashSet<String> postfixTypes) {
    for (String type : usedTypes){
      int index = type.indexOf(':');
      if (index == -1)
        prefixTypes.add(type);
      else {
        prefixTypes.add(type.substring(0,index));
        postfixTypes.add(type.substring(index+1));

      }
    }
  }


  public String getTitle() {
    return title;
  }


  public void stateChanged(ChangeEvent e) {
    updateTypesList();
    repaint();
  }


  private void updateTypesList() {

    HashSet<String> prefixTypes = new HashSet<String>();
    HashSet<String> postfixTypes = new HashSet<String>();
    separateTypes(nlpCanvas.getUsedTypes(), prefixTypes, postfixTypes);
    ArrayList<String> allTypes = new ArrayList<String>();
    allTypes.addAll(prefixTypes);
    allTypes.addAll(postfixTypes);

    ListSelectionModel selectionModel = new DefaultListSelectionModel(); //types.getSelectionModel();
    listModel.clear();
//    for (Object item : listModel.toArray())
//      if (!allTypes.contains(item.toString()))
//        listModel.removeElement(item);
    int index = 0;
    for (String type : allTypes) {
      if (!listModel.contains(type)){
        listModel.addElement(type);
        if (nlpCanvas.getDependencyTypeFilter().allows(type))
          selectionModel.addSelectionInterval(index,index);
      }
      ++index;
    }
    types.setSelectionModel(selectionModel);
  }


  public void instanceChanged() {
    updateTypesList();
    repaint();
  }

  public void redrawn() {

  }

  public void changed(String type) {
    
  }
}
