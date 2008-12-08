package com.googlecode.whatswrong;

import com.googlecode.whatswrong.javautils.Pair;
import org.apache.lucene.analysis.Analyzer;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;

/**
 * A CorpusNavigator allows the user to navigate through a corpus (or a diffed
 * corpus) and pick one NLP instance to draw (or one difference of two
 * NLPInstance objects in terms of their edges). The CorpusNavigator also allows
 * us to search a corpus for keywords by using the Lucene IR engine. The
 * instances that match the user's query are presented in a list and one of them
 * can then be picked to be rendered. The CorpusNavigator has also a spinner
 * panel that allows to go through this corpus by index. This spinner is not
 * part of the navigator panel and can be placed anywhere.
 *
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MissingMethodJavaDoc"})
public class CorpusNavigator extends JPanel implements CorpusLoader.Listener {

  /**
   * The loader for guess instances.
   */
  private CorpusLoader guess;
  /**
   * The loader for gold instances.
   */
  private CorpusLoader gold;
  /**
   * The canvas that renders the instances.
   */
  private NLPCanvas canvas;
  /**
   * The spinner that controls the current instance to be rendered by the
   * canvas.
   */
  private JSpinner spinner;
  /**
   * The number model that backs the spinner that controls the current instance
   * to render.
   */
  private SpinnerNumberModel numberModel;

  /**
   * A mapping from corpora to index searchers that can be used to search the
   * corpus.
   */
  private HashMap<List<NLPInstance>, IndexSearcher>
    indices = new HashMap<List<NLPInstance>, IndexSearcher>();

  /**
   * A mapping from pairs of corpora to index searchers that can be used to
   * search the differences between the two corpora.
   */
  private HashMap<Pair<List<NLPInstance>, List<NLPInstance>>, List<NLPInstance>>
    diffCorpora = new HashMap<Pair<List<NLPInstance>, List<NLPInstance>>, List<NLPInstance>>();
  //private HashMap<List<NLPInstance>>

  /**
   * The set of gold corpora.
   */
  private HashSet<List<NLPInstance>>
    goldCorpora = new HashSet<List<NLPInstance>>();
  /**
   * The set of guess corpora.
   */
  private HashSet<List<NLPInstance>>
    guessCorpora = new HashSet<List<NLPInstance>>();

  /**
   * The current IndexSearcher (for the selected corpus/corpus pair).
   */
  private IndexSearcher indexSearcher;
  /**
   * The Analyzer for the search index.
   */
  private Analyzer analyzer;
  /**
   * The search button that triggers the search process.
   */
  private JButton searchButton;
  /**
   * The list of search results.
   */
  private JList results;
  /**
   * The field for the search terms.
   */
  private JTextField search;
  /**
   * The NLPDiff object that compares pairs of instances.
   */
  private NLPDiff diff = new NLPDiff();
  /**
   * The panel that controls the instance index spinner.
   */
  private JPanel spinnerPanel;
  /**
   * The label that shows how many results where found.
   */
  private JLabel ofHowMany;

  /**
   * The EdgeTypeFilter that needs to be initialized when the navigator does not
   * have a selected corpus and shows an example sentence.
   */
  private EdgeTypeFilter edgeTypeFilter;

  /**
   * Adds the corpus to the corresponding internal set of corpora.
   *
   * @param corpus the corpus to add.
   * @param src    the source loader.
   */
  public void corpusAdded(final List<NLPInstance> corpus,
                          final CorpusLoader src) {
    if (src == gold) {
      goldCorpora.add(corpus);
      //indices.put(corpus, createIndex(corpus));
    } else {
      guessCorpora.add(corpus);
      //indices.put(corpus, createIndex(corpus));
    }
  }

  /**
   * Returns a difference corpus between two corpora. This difference corpus is
   * calculated if it hasn't been calculated before.
   *
   * @param gold  the gold corpus.
   * @param guess the guess corpus.
   * @return the difference corpus.
   * @see com.googlecode.whatswrong.NLPDiff
   */
  private List<NLPInstance> getDiffCorpus(final List<NLPInstance> gold,
                                          final List<NLPInstance> guess) {
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

  /**
   * Removes the difference corpus for the given corpus pair.
   *
   * @param gold  the gold corpus.
   * @param guess the guess corpus.
   */
  private void removeDiffCorpus(final List<NLPInstance> gold,
                                final List<NLPInstance> guess) {
    Pair<List<NLPInstance>, List<NLPInstance>> pair = new Pair<List<NLPInstance>, List<NLPInstance>>(gold, guess);
    List<NLPInstance> diffCorpus = diffCorpora.get(pair);
    if (diffCorpus != null) {
      diffCorpora.remove(pair);
      indices.remove(diffCorpus);
    }
  }

  /**
   * Removes the corpus and all diff corpora that compare the given corpus
   *
   * @param corpus the corpus to remove.
   * @param src    the loader that removed the corpus.
   */
  public void corpusRemoved(final List<NLPInstance> corpus,
                            final CorpusLoader src) {
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

  /**
   * Changes the current selected instance to be the one in the new corpus with
   * the same index as the last chosen instance of the old corpus or the last
   * instance if no such instance exist.
   *
   * @param corpus the newly selected corpus.
   * @param src    the loader in which the corpus was selected.
   */
  public synchronized void corpusSelected(final List<NLPInstance> corpus,
                                          final CorpusLoader src) {
    updateCanvas();
    results.setModel(new DefaultListModel());

  }

  /**
   * A Search result consisting of the instance index and a text snippet that
   * indicates the position in the instance where they key terms were found.
   */
  private static class Result {
    /**
     * A text representation of the location in which the key terms were found.
     */
    public final String text;
    /**
     * The index of the instance in which the key terms were found.
     */
    public final int nr;

    /**
     * Creates a new Result.
     *
     * @param nr   the index nr.
     * @param text the text snippet.
     */
    public Result(final int nr, final String text) {
      this.text = text;
      this.nr = nr;
    }

    /**
     * Returns the text snippet.
     *
     * @return the text snippet.
     */
    public String toString() {
      return text;
    }
  }

  /**
   * Creates a new CorpusNavigator.
   *
   * @param canvas         the canvas to control.
   * @param goldLoader     the loader of gold corpora.
   * @param guessLoader    the loader of guess corpora.
   * @param edgeTypeFilter the EdgeTypeFilter we need when no corpus is selected
   *                       and a example sentence is chosen and passed to the
   *                       NLPCanvas.
   */
  public CorpusNavigator(final NLPCanvas canvas,
                         final CorpusLoader goldLoader,
                         final CorpusLoader guessLoader,
                         final EdgeTypeFilter edgeTypeFilter) {
    super(new GridBagLayout());
    this.edgeTypeFilter = edgeTypeFilter;
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
    JScrollPane resultsPane = new JScrollPane(results);
    resultsPane.setMinimumSize(new Dimension(100, 10));
    add(resultsPane, new SimpleGridBagConstraints(0, 1, 2, 2));

    //setPreferredSize((new Dimension(100, (int) getPreferredSize().getHeight())));
    analyzer = new WhitespaceAnalyzer();
    updateCanvas();
    //analyzer.
  }


  /**
   * Returns the panel that contains the spinner to set the instance nr.
   *
   * @return the panel that contains the spinner to set the instance nr.
   */
  public JPanel getSpinnerPanel() {
    return spinnerPanel;
  }

  /**
   * Searches the current corpus using the search terms in the search field.
   */
  private void searchCorpus() {
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
        if (best != null)
          model.addElement(new Result(nr, "<html>" + nr + ":" + best + "</html>"));
        //System.out.println(highlighter.getBestFragment(analyzer, "Word", hitDoc.get("Word")));
        //assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
      }
      results.setModel(model);
      repaint();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**
   * Returns an IndexSearcher for the given corpus. A new one is created if not
   * yet existent.
   *
   * @param corpus the corpus to get an IndexSearcher for.
   * @return the IndexSearcher for the given corpus.
   */
  private synchronized IndexSearcher getIndex(final List<NLPInstance> corpus) {
    IndexSearcher index = indices.get(corpus);
    if (index == null) {
      index = createIndex(corpus);
      indices.put(corpus, index);
    }
    return index;
  }

  /**
   * Creates an IndexSearcher for the given corpus that allows us to search the
   * corpus efficiently for keywords in the token properties and edges.
   *
   * @param corpus the corpus to create the IndexSearcher for.
   * @return An IndexSearcher for the given corpus.
   */
  private IndexSearcher createIndex(final List<NLPInstance> corpus) {
    try {
      System.err.println("Creating Index");
      RAMDirectory directory = new RAMDirectory();
      IndexWriter iwriter;
      iwriter = new IndexWriter(directory, analyzer, true);
      iwriter.setMaxFieldLength(25000);

      int nr = 0;
      for (NLPInstance instance : corpus) {
        Document doc = new Document();
        HashMap<TokenProperty, StringBuffer>
          sentences = new LinkedHashMap<TokenProperty, StringBuffer>();
        for (Token token : instance.getTokens()) {
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
          doc.add(new Field(p.getName(), sentences.get(p).toString(),
            Field.Store.YES, Field.Index.TOKENIZED));
        }

        //edges
        HashMap<String, StringBuffer> edges = new HashMap<String, StringBuffer>();
        StringBuffer types = new StringBuffer();
        for (Edge e : instance.getEdges()) {
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

  /**
   * Updates the canvas based on the current state of the navigator and the
   * corpus loaders.
   */
  private void updateCanvas() {
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
        canvas.getSpanLayout().setColor("FN", Color.BLUE);
        canvas.getSpanLayout().setColor("FP", Color.RED);
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
      example.addSpan(1, 1, "add.1", "sense");
//      canvas.getDependencyLayout().setStroke("role", new BasicStroke(1.0f,
//        BasicStroke.CAP_BUTT,
//        BasicStroke.JOIN_BEVEL, 10,
//        new float[]{2.0f}, 0));
      canvas.setNLPInstance(example);

      edgeTypeFilter.addAllowedPrefixType("dep");
      edgeTypeFilter.addAllowedPrefixType("role");
      edgeTypeFilter.addAllowedPrefixType("sense");
      edgeTypeFilter.addAllowedPrefixType("ner");
      edgeTypeFilter.addAllowedPrefixType("chunk");
      edgeTypeFilter.addAllowedPrefixType("pos");
      edgeTypeFilter.addAllowedPostfixType("FP");
      edgeTypeFilter.addAllowedPostfixType("FN");
      edgeTypeFilter.addAllowedPostfixType("Match");

      canvas.getSpanLayout().setTypeOrder("pos", 0);
      canvas.getSpanLayout().setTypeOrder("chunk (BIO)", 1);
      canvas.getSpanLayout().setTypeOrder("chunk", 2);
      canvas.getSpanLayout().setTypeOrder("ner (BIO)", 2);
      canvas.getSpanLayout().setTypeOrder("ner", 3);
      canvas.getSpanLayout().setTypeOrder("sense", 4);
      canvas.getSpanLayout().setTypeOrder("role", 5);
      canvas.getSpanLayout().setTypeOrder("phrase", 5);

      canvas.updateNLPGraphics();

    }
  }
}
