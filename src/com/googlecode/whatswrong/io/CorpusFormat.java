package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import javax.swing.*;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.IOException;

/**
 * The CorpusFormat interface describes objects that can load a list of
 * NLPInstances from a file. The Corpus can also provide a GUI element that
 * allows the user to configure how the file is to be loaded.
 *
 * @author Sebastian Riedel
 */
public interface CorpusFormat {

  /**
   * Returns the name of this format.
   *
   * @return the name of this format.
   */
  String getName();

  /**
   * Returns the GUI element that controls how this format is to be loaded.
   *
   * @return the GUI element that controls how this format is to be loaded.
   */
  JComponent getAccessory();

  /**
   * Sets the objects that monitors the progress of this format when loading a
   * file.
   *
   * @param monitor the monitor for this format.
   */
  void setMonitor(Monitor monitor);

  /**
   * Loads a configuration for this format from the given Properties object.
   *
   * @param properties the Properties object to load from.
   * @param prefix     the prefix that properties for this format have in the
   *                   Properties object.
   */
  void loadProperties(Properties properties, String prefix);

  /**
   * Saves the configuration of this format to a Properties object.
   *
   * @param properties the Properties object to store this configuration of this
   *                   format to.
   * @param prefix     the prefix that the properties should have.
   */
  void saveProperties(Properties properties, String prefix);

  /**
   * Loads a corpus from a file, starting at instance <code>from</code> and
   * ending at instance <code>to</code> (exclusive). This method is required to
   * call {@link com.googlecode.whatswrong.io.CorpusFormat.Monitor#progressed(int)}
   * after each instance that was processed.
   *
   * @param file the file to load the corpus from.
   * @param from the starting instance index.
   * @param to   the end instance index.
   * @return a list of NLP instances loaded from the given file in the given
   *         interval.
   * @throws IOException if I/O goes wrong.
   */
  List<NLPInstance> load(File file, int from, int to) throws IOException;

  /**
   * A Monitor monitors the progress of the {@link com.googlecode.whatswrong.io.CorpusFormat#load(java.io.File,
   * int, int)} method.
   */
  static interface Monitor {
    /**
     * Called whenever one instance was processed in loading of the file.
     *
     * @param index the index of the processed instance.
     */
    void progressed(int index);
  }

}
