package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;
import com.googlecode.whatswrong.SimpleGridBagConstraints;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * A CoNLLFormat loads CoNLL type data. The format for each individual CoNLL
 * shared task has to be implemented through a {@link com.googlecode.whatswrong.io.CoNLLProcessor}.
 *
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MissingMethodJavaDoc", "MissingFieldJavaDoc"})
public class CoNLLFormat implements CorpusFormat {

  private JPanel accessory;
  private SortedMap<String, CoNLLProcessor> processors = new TreeMap<String, CoNLLProcessor>();
  private JComboBox year;
  private JCheckBox open;
  private Monitor monitor;


  public CoNLLFormat() {
    addProcessor("2008", new CoNLL2008());
    addProcessor("2006", new CoNLL2006());
    addProcessor("2004", new CoNLL2004());
    addProcessor("2002", new CoNLL2002());
    addProcessor("2003", new CoNLL2003());
    addProcessor("2000", new CoNLL2000());

    accessory = new JPanel(new GridBagLayout());
    year = new JComboBox(new Vector<Object>(processors.values()));
    year.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        open.setEnabled(((CoNLLProcessor) year.getSelectedItem()).supportsOpen());
      }
    });
    open = new JCheckBox("open", false);
    open.setToolTipText("If checked an additional file with same name but .open extension is also loaded");
    open.setEnabled(((CoNLLProcessor) year.getSelectedItem()).supportsOpen());

    accessory.add(new JLabel("Year:"), new SimpleGridBagConstraints(0, true));
    accessory.add(year, new SimpleGridBagConstraints(0, false));
    accessory.add(open, new SimpleGridBagConstraints(1, false));

  }

  public void addProcessor(String name, CoNLLProcessor processor) {
    processors.put(name, processor);
  }

  public String toString() {
    return getName();
  }

  public String getName() {
    return "CoNLL";
  }

  public JComponent getAccessory() {
    return accessory;
  }

  public void setMonitor(Monitor monitor) {
    this.monitor = monitor;
  }

  public void loadProperties(Properties properties, String prefix) {
    String yearString = properties.getProperty(prefix + ".conll.year", "2008");
    year.setSelectedItem(processors.get(yearString));
  }


  public void saveProperties(Properties properties, String prefix) {
    properties.setProperty(prefix + ".conll.year", year.getSelectedItem().toString());

  }


  public java.util.List<NLPInstance> load(File file, int from, int to) throws IOException {
    CoNLLProcessor processor = (CoNLLProcessor) year.getSelectedItem();
    java.util.List<NLPInstance> result = loadCoNLL08(file, from, to, processor, false);
    if (open.isSelected()) {
      String filename = file.getName().substring(0, file.getName().lastIndexOf('.')) + ".open";
      File openFile = new File(file.getParent() + "/" + filename);
      java.util.List<NLPInstance> openCorpus = loadCoNLL08(openFile, from, to, processor, true);
      for (int i = 0; i < openCorpus.size(); ++i) {
        result.get(i).merge(openCorpus.get(i));
      }

    }
    return result;
  }

  private java.util.List<NLPInstance> loadCoNLL08(File file, int from, int to, CoNLLProcessor processor, boolean open) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    ArrayList<NLPInstance> corpus = new ArrayList<NLPInstance>();
    ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
    int instanceNr = 0;
    for (String line = reader.readLine(); line != null && instanceNr < to; line = reader.readLine()) {
      line = line.trim();
      if (line.equals("")) {
        monitor.progressed(instanceNr);
        if (instanceNr++ < from) continue;
        NLPInstance instance = open ? processor.createOpen(rows) : processor.create(rows);
        corpus.add(instance);
        rows.clear();
      } else {
        if (instanceNr < from) continue;
        StringTokenizer tokenizer = new StringTokenizer(line, "[ \t]");
        ArrayList<String> row = new ArrayList<String>();
        while (tokenizer.hasMoreElements()) row.add(tokenizer.nextToken());
        rows.add(row);
      }

    }
    if (rows.size() > 0)
      corpus.add(open ? processor.createOpen(rows) : processor.create(rows));
    return corpus;

  }

  public static void extractSpan03(java.util.List<? extends java.util.List<String>> rows,
                                   int column,
                                   String type,
                                   NLPInstance instance) {
    int index;
    index = 0;
    boolean inChunk = false;
    int begin = 0;
    String currentChunk = "";
    for (java.util.List<String> row : rows) {
      String chunk = row.get(column);
      int minus = chunk.indexOf('-');
      if (minus != -1) {
        String bio = chunk.substring(0, minus);
        String label = chunk.substring(minus + 1);
        if (inChunk) {
          //start a new chunk and finish old one
          if ("B".equals(bio) || "I".equals(bio) && !label.equals(currentChunk)) {
            instance.addSpan(begin, index - 1, currentChunk, type);
            begin = index;
            currentChunk = label;
          }
        } else {
          inChunk = true;
          begin = index;
          currentChunk = label;
        }
      } else {
        if (inChunk) {
          instance.addSpan(begin, index - 1, currentChunk, type);
          inChunk = false;
        }
      }
      ++index;
    }
    if (inChunk) {
      instance.addSpan(begin, index - 1, currentChunk, type);
    }
  }


  public static void extractSpan00(java.util.List<? extends java.util.List<String>> rows,
                                   int column,
                                   String type,
                                   NLPInstance instance) {
    int index;
    index = 0;
    boolean inChunk = false;
    int begin = 0;
    String currentChunk = "";
    for (java.util.List<String> row : rows) {
      String chunk = row.get(column);
      int minus = chunk.indexOf('-');
      if (minus != -1) {
        String bio = chunk.substring(0, minus);
        String label = chunk.substring(minus + 1);
        if ("B".equals(bio)) {
          if (inChunk) {
            instance.addSpan(begin, index - 1, currentChunk, type);
          }
          begin = index;
          currentChunk = label;
          inChunk = true;
        }
      } else {
        if (inChunk) {
          instance.addSpan(begin, index - 1, currentChunk, type);
          inChunk = false;
        }
      }
      ++index;
    }
  }

  public static void extractSpan05(java.util.List<? extends java.util.List<String>> rows,
                                   int column,
                                   String type,
                                   String prefix,
                                   NLPInstance instance) {
    int index = 0;
    int begin = 0;
    String currentChunk = "";
    for (java.util.List<String> row : rows) {
      String chunk = row.get(column);
      if (chunk.startsWith("(")) {
        currentChunk = chunk.substring(1, chunk.indexOf("*"));
        begin = index;
      }
      if (chunk.endsWith(")")) {
        instance.addSpan(begin, index, prefix + currentChunk, type);
      }
      ++index;
    }
  }

}

