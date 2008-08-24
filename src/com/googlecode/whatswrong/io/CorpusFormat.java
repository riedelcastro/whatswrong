package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import javax.swing.*;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.IOException;

/**
 * @author Sebastian Riedel
 */

public interface CorpusFormat {

  String getName();

  JComponent getAccessory();

  void setMonitor(Monitor monitor);

  void loadProperties(Properties properties, String prefix);

  void saveProperties(Properties properties, String prefix);

  List<NLPInstance> load(File file, int from, int to) throws IOException;

  interface Monitor {
    void progressed(int index);
  }

}
