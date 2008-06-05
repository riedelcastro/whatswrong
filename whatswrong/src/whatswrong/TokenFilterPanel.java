package whatswrong;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Sebastian Riedel
 */
public class TokenFilterPanel extends ControllerPanel implements ChangeListener {
  private DefaultListModel listModel;
  private final JList list;
  private NLPCanvas canvas;
  private HashSet<TokenProperty> properties = new HashSet<TokenProperty>();


  public TokenFilterPanel(final NLPCanvas canvas) {
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
        TokenFilter filter = canvas.getTokenFilter();
        if (e.getFirstIndex() == -1 || list.getModel().getSize() == 0)
          return;
        for (int index = e.getFirstIndex(); index < e.getLastIndex() + 1; ++index) {
          if (list.isSelectedIndex(index)) {
            filter.removeForbiddenProperty(listModel.get(index).toString());
          } else {
            filter.addForbiddenProperty(listModel.get(index).toString());
          }
        }
        canvas.updateNLPGraphics();
      }

    });

    JScrollPane pane = new JScrollPane(list);
    add(pane);

    add(new JSeparator());
    JPanel allowedTokens = new JPanel(new GridBagLayout());
    allowedTokens.add(new JLabel("Token:"), new SimpleGridBagConstraints(0,true));
    final JTextField allowed = new JTextField();

    allowed.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent event) {
        canvas.getTokenFilter().clearAllowedStrings();
        String[] split = allowed.getText().trim().split("[,]");
        for (String property : split)
          canvas.getTokenFilter().addAllowedString(property);
        canvas.updateNLPGraphics();
      }
    });

    allowedTokens.add(allowed, new SimpleGridBagConstraints(0,false));
    allowedTokens.add(new JLabel("Options:"), new SimpleGridBagConstraints(1,true));
    final JCheckBox wholeWords = new JCheckBox("Whole Words");
    wholeWords.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        canvas.getTokenFilter().setWholeWord(wholeWords.isSelected());
        canvas.updateNLPGraphics();
      }
    });
    allowedTokens.add(wholeWords, new SimpleGridBagConstraints(1,false));


    add(allowedTokens);
    pane.setPreferredSize(new Dimension(150, 100));
    pane.setMinimumSize(new Dimension(150, 100));


  }

  private void updateProperties() {
    ArrayList<TokenProperty> sorted = new ArrayList<TokenProperty>(this.canvas.getUsedProperties());
    Collections.sort(sorted);
    properties = new HashSet<TokenProperty>(sorted);
    int index = 0;
    listModel.clear();
    listModel = new DefaultListModel();
    DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
    for (TokenProperty p : sorted) {
      listModel.addElement(p);
      if (!this.canvas.getTokenFilter().getForbiddenTokenProperties().contains(p)
              && !list.isSelectedIndex(index))
        selectionModel.addSelectionInterval(index, index);
      ++index;
    }
    list.setModel(listModel);
    list.setSelectionModel(selectionModel);

  }

  public void stateChanged(ChangeEvent e) {
    updateProperties();
    repaint();


  }
}
