package whatswrong;

import javautils.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.store.RAMDirectory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author Sebastian Riedel
 */
public class CorpusNavigator extends JPanel implements CorpusLoader.Listener {

  private CorpusLoader guess, gold;
  private NLPCanvas canvas;
  private JSpinner spinner;
  private SpinnerNumberModel numberModel;
  private HashMap<List<NLPInstance>, IndexSearcher>
    indices = new HashMap<List<NLPInstance>, IndexSearcher>();
  private HashMap<Pair<List<NLPInstance>, List<NLPInstance>>, List<NLPInstance>>
    diffCorpora = new HashMap<Pair<List<NLPInstance>, List<NLPInstance>>, List<NLPInstance>>();
  //private HashMap<List<NLPInstance>>
  private HashSet<List<NLPInstance>>
    goldCorpora = new HashSet<List<NLPInstance>>(),
    guessCorpora = new HashSet<List<NLPInstance>>();
  private IndexSearcher indexSearcher;
  private Analyzer analyzer;
  private JButton searchButton;
  private JList results;
  private JTextField search;
  private NLPDiff diff = new NLPDiff();
  private JPanel spinnerPanel;
  private JLabel ofHowMany;

  public void corpusAdded(List<NLPInstance> corpus, CorpusLoader src) {
    if (src == gold) {
      goldCorpora.add(corpus);
      //indices.put(corpus, createIndex(corpus));
    } else {
      guessCorpora.add(corpus);
      //indices.put(corpus, createIndex(corpus));
    }
  }

  private List<NLPInstance> getDiffCorpus(List<NLPInstance> gold, List<NLPInstance> guess) {
    List<NLPInstance> diffCorpus = diffCorpora.get(new Pair<List<NLPInstance>, List<NLPInstance>>(gold, guess));
    if (diffCorpus == null) {
      diffCorpus = new ArrayList<NLPInstance>(Math.min(gold.size(), guess.size()));
      diffCorpora.put(new Pair<List<NLPInstance>, List<NLPInstance>>(gold, guess), diffCorpus);
      for (int i = 0; i < Math.min(gold.size(), guess.size()); ++i)
        diffCorpus.add(diff.diff(gold.get(i), guess.get(i)));
      //indices.put(diffCorpus,createIndex(diffCorpus));
    }
    return diffCorpus;
  }

  private void removeDiffCorpus(List<NLPInstance> gold, List<NLPInstance> guess) {
    Pair<List<NLPInstance>, List<NLPInstance>> pair = new Pair<List<NLPInstance>, List<NLPInstance>>(gold, guess);
    List<NLPInstance> diffCorpus = diffCorpora.get(pair);
    if (diffCorpus != null) {
      diffCorpora.remove(pair);
      indices.remove(diffCorpus);
    }
  }

  public void corpusRemoved(List<NLPInstance> corpus, CorpusLoader src) {
    if (src == gold) {
      goldCorpora.remove(corpus);
      indices.remove(corpus);
      for (List<NLPInstance> c : guessCorpora) {
        removeDiffCorpus(corpus, c);
      }
    } else {
      guessCorpora.remove(corpus);
      indices.remove(corpus);
      for (List<NLPInstance> c : goldCorpora) {
        removeDiffCorpus(corpus, c);
      }
    }

  }

  public synchronized void corpusSelected(List<NLPInstance> corpus, CorpusLoader src) {
    updateCanvas();
    results.setModel(new DefaultListModel());

  }

  private static class Result {
    public final String text;
    public final int nr;

    public Result(int nr, String text) {
      this.text = text;
      this.nr = nr;
    }

    public String toString() {
      return text;
    }
  }

  public CorpusNavigator(NLPCanvas canvas, CorpusLoader goldLoader, CorpusLoader guessLoader) {
    super(new GridBagLayout());
    this.guess = guessLoader;
    this.gold = goldLoader;
    this.canvas = canvas;
    guessLoader.addChangeListener(this);
    goldLoader.addChangeListener(this);
    setBorder(new EmptyBorder(5, 5, 5, 5));
    //setBorder(new TitledBorder(new EtchedBorder(), "Navigate"));

    numberModel = new SpinnerNumberModel();
    numberModel.setMinimum(0);
    numberModel.setMaximum(100);
    spinner = new JSpinner(numberModel);
    //spinner.getEditor().set
    spinner.setEnabled(false);
    JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(spinner);
    spinner.setEditor(numberEditor);
    spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        updateCanvas();
      }
    });

    final JFormattedTextField editorTextField = numberEditor.getTextField();
    editorTextField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          spinner.setValue(Integer.valueOf(editorTextField.getText()));
        } catch (NumberFormatException ex) {
          spinner.setValue(editorTextField.getValue());
        }
      }
    });


    spinnerPanel = new JPanel();
    spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.X_AXIS));
    spinnerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    spinnerPanel.add(spinner);
    ofHowMany = new JLabel(" of 1");
    spinnerPanel.add(ofHowMany);

    search = new JTextField(10);
    search.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        searchCorpus();
      }
    });
    //add(search, new SimpleGridBagConstraints(1, false));

    //search button
    searchButton = new JButton("Search");
    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.add(search, BorderLayout.CENTER);
    searchPanel.add(searchButton, BorderLayout.EAST);
    //add(searchButton, new SimpleGridBagConstraints(2, false, false));
    searchButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        searchCorpus();
      }
    });

    results = new JList();
    results.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        int selectedIndex = results.getSelectedIndex();
        if (selectedIndex != -1) {
          int nr = ((Result) results.getSelectedValue()).nr;
          spinner.setValue(nr);
          repaint();
        }
      }
    });


    add(searchPanel, new SimpleGridBagConstraints(0, 0, 2, 1));
    add(new JScrollPane(results), new SimpleGridBagConstraints(0, 1, 2, 1));

    //setPreferredSize((new Dimension(100, (int) getPreferredSize().getHeight())));
    analyzer = new WhitespaceAnalyzer();
    updateCanvas();
    //analyzer.
  }


  public JPanel getSpinnerPanel
    () {
    return spinnerPanel;
  }

  private void searchCorpus
    () {
    if (search.getText().trim().equals("")) return;
    try {
      indexSearcher = guess.getSelected() != null ?
        getIndex(getDiffCorpus(gold.getSelected(), guess.getSelected())) :
        getIndex(gold.getSelected());
      //System.out.println("Searching...");
      QueryParser parser = new QueryParser("Word", analyzer);
      Query query = parser.parse(search.getText());
      Hits hits = indexSearcher.search(query);
      Highlighter highlighter = new Highlighter(new QueryScorer(query));
      DefaultListModel model = new DefaultListModel();
      for (int i = 0; i < hits.length(); i++) {
        Document hitDoc = hits.doc(i);
        int nr = Integer.parseInt(hitDoc.get("<nr>"));
        //System.out.println(hitDoc.get("<nr>"));
        String best = null;
        for (Object field : hitDoc.getFields()) {
          Field f = (Field) field;
          best = highlighter.getBestFragment(analyzer, f.name(), hitDoc.get(f.name()));
          if (best != null) break;
        }
        if (best != null) model.addElement(new Result(nr, "<html>" + nr + ":" + best + "</html>"));
        //System.out.println(highlighter.getBestFragment(analyzer, "Word", hitDoc.get("Word")));
        //assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
      }
      results.setModel(model);
      repaint();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  private synchronized IndexSearcher getIndex
    (List<NLPInstance> corpus) {
    IndexSearcher index = indices.get(corpus);
    if (index == null) {
      index = createIndex(corpus);
      indices.put(corpus, index);
    }
    return index;
  }

  private IndexSearcher createIndex
    (List<NLPInstance> corpus) {
    try {
      System.err.println("Creating Index");
      RAMDirectory directory = new RAMDirectory();
      IndexWriter iwriter;
      iwriter = new IndexWriter(directory, analyzer, true);
      iwriter.setMaxFieldLength(25000);

      int nr = 0;
      for (NLPInstance instance : corpus) {
        Document doc = new Document();
        HashMap<TokenProperty, StringBuffer> sentences = new HashMap<TokenProperty, StringBuffer>();
        for (TokenVertex token : instance.getTokens()) {
          for (TokenProperty p : token.getPropertyTypes()) {
            StringBuffer buffer = sentences.get(p);
            if (buffer == null) {
              buffer = new StringBuffer();
              sentences.put(p, buffer);
            }
            if (token.getIndex() > 0) buffer.append(" ");
            buffer.append(token.getProperty(p));
          }
        }
        for (TokenProperty p : sentences.keySet()) {
          doc.add(new Field(p.getName(), sentences.get(p).toString(), Field.Store.YES, Field.Index.TOKENIZED));
        }

        //edges
        HashMap<String, StringBuffer> edges = new HashMap<String, StringBuffer>();
        StringBuffer types = new StringBuffer();
        for (DependencyEdge e : instance.getDependencies()) {
          String prefix = e.getTypePrefix();
          StringBuffer prefixBuffer = edges.get(prefix);
          types.append(prefix).append(" ");
          if (prefixBuffer == null) {
            prefixBuffer = new StringBuffer();
            edges.put(prefix, prefixBuffer);
          }
          prefixBuffer.append(e.getLabel()).append(" ");
          String postfix = e.getTypePostfix();
          if (postfix != null) {
            types.append(postfix).append(" ");
            StringBuffer postfixBuffer = edges.get(postfix);
            if (postfixBuffer == null) {
              postfixBuffer = new StringBuffer();
              edges.put(postfix, postfixBuffer);
            }
            postfixBuffer.append(e.getLabel()).append(" ");
          }
        }

        doc.add(new Field("types", types.toString(), Field.Store.YES, Field.Index.TOKENIZED));

        for (String type : edges.keySet()) {
          doc.add(new Field(type, edges.get(type).toString(), Field.Store.YES, Field.Index.TOKENIZED));
        }

        //for (DependencyEdge e : instance.getTokens())
        doc.add(new Field("<nr>", String.valueOf(nr), Field.Store.YES, Field.Index.UN_TOKENIZED));

        System.err.print(".");
        iwriter.addDocument(doc);
        nr++;
      }
      System.err.println();
      iwriter.optimize();
      iwriter.close();
      return new IndexSearcher(directory);
    } catch (IOException e) {
      throw new RuntimeException("Couldn't build the index");
    }

  }

  private void updateCanvas
    () {
    if (gold.getSelected() != null) {
      searchButton.setEnabled(true);
      search.setEnabled(true);
      spinner.setEnabled(true);
      results.setEnabled(true);
      if (guess.getSelected() == null) {
        int maxIndex = gold.getSelected().size() - 1;
        int index = Math.min((Integer) spinner.getValue(), maxIndex);
        spinner.setValue(index);
        numberModel.setMaximum(maxIndex);
        ofHowMany.setText(" of " + maxIndex);

        indexSearcher = getIndex(gold.getSelected());
        canvas.setNLPInstance(gold.getSelected().get(index));
        canvas.updateNLPGraphics();
      } else {
        int maxIndex = Math.min(gold.getSelected().size() - 1, guess.getSelected().size() - 1);
        numberModel.setMaximum(maxIndex);
        int index = Math.min((Integer) spinner.getValue(), maxIndex);
        spinner.setValue(index);
        ofHowMany.setText(" of " + maxIndex);
        NLPInstance instance = getDiffCorpus(gold.getSelected(), guess.getSelected()).get(index);
        canvas.getDependencyLayout().setColor("FN", Color.BLUE);
        canvas.getDependencyLayout().setColor("FP", Color.RED);
        canvas.setNLPInstance(instance);
        canvas.updateNLPGraphics();

      }
    } else {
      searchButton.setEnabled(false);
      search.setEnabled(false);
      spinner.setEnabled(false);
      spinner.setValue(0);
      searchButton.setEnabled(false);
      results.setEnabled(false);
      ofHowMany.setText(" of 1");


      NLPInstance example = new NLPInstance();
      example.addToken().addProperty("Word", "[root]").addProperty("Index", "0");
      example.addToken().addProperty("Word", "Add").addProperty("Index", "1");
      example.addToken().addProperty("Word", "a").addProperty("Index", "2");
      example.addToken().addProperty("Word", "gold").addProperty("Index", "3");
      example.addToken().addProperty("Word", "corpus").addProperty("Index", "4");
      example.addToken().addProperty("Word", "!").addProperty("Index", "5");
      example.addDependency(0, 1, "ROOT", "dep");
      example.addDependency(0, 5, "PUNC", "dep");
      example.addDependency(1, 4, "OBJ", "dep");
      example.addDependency(4, 2, "DET", "dep");
      example.addDependency(4, 3, "MOD", "dep");
      example.addDependency(1, 4, "A1", "role");
      canvas.getDependencyLayout().setStroke("role", new BasicStroke(1.0f,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL, 10,
        new float[]{2.0f}, 0));
      canvas.setNLPInstance(example);
      canvas.updateNLPGraphics();

    }
  }
}
