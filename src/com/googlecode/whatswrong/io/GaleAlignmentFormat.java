package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import javax.swing.*;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Sebastian Riedel
 */
public class GaleAlignmentFormat implements CorpusFormat {
  public String getName() {
    return "Gale Alignment";
  }

  public JComponent getAccessory() {
    return new JPanel();
  }

  public void setMonitor(Monitor monitor) {

  }

  public void loadProperties(Properties properties, String prefix) {

  }

  public void saveProperties(Properties properties, String prefix) {

  }

  @SuppressWarnings({"ConstantConditions"})
  public List<NLPInstance> load(File file,
                                int from,
                                int to) throws IOException {
    ArrayList<NLPInstance> result = new ArrayList<NLPInstance>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    NLPInstance instance = null;
    int sourceLength = -1;
    int targetLength = -1;
    for (String line = reader.readLine();
         line != null; line = reader.readLine()) {
      if (line.startsWith("<source>")){
        String content = line.trim().substring(8,line.length()-9);
        for (String token : content.split("\\s+")){
          instance.addToken().addProperty("word",token);
        }
        sourceLength = instance.getTokens().size();
        instance.addSplitPoint(sourceLength);
      } else if (line.startsWith("<seg")){
        instance = new NLPInstance();
        instance.setRenderType(NLPInstance.RenderType.alignment);
      } else if (line.startsWith("<translation>")){
        String content = line.trim().substring(13,line.length()-14);
        for (String token : content.split("\\s+")){
          instance.addToken().addProperty("word",token);
        }
        targetLength = instance.getTokens().size() - sourceLength;
      } else if (line.startsWith("<matrix>")){
        reader.readLine();
        for (int tgt = 0; tgt < targetLength; ++tgt){
          line = reader.readLine();
          String[] col = line.split("\\s+");
          for (int src = 1 ; src < col.length; ++src){
            if (col[src].equals("1"))
              instance.addEdge(src - 1, tgt + sourceLength, "align", "align");
          }
        }
        result.add(instance);
      } 
    }
    return result;
  }

  public String toString() {
    return getName();
  }
}
