package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;

/**
 * Loads CoNLL 2006 Dependency data.
 *
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MissingFieldJavaDoc"})
public class CoNLL2006 implements TabProcessor {

  /**
   * The name of the processor.
   */
  public static final String name = "CoNLL 2006";

  /**
   * Returns the name of this processor.
   *
   * @return the name of this processor.
   */  
  public String toString() {
    return name;
  }

  /**
   * @see TabProcessor#create(List<? extends List<String>>)
   */
  public NLPInstance create(List<? extends List<String>> rows) {

    NLPInstance instance = new NLPInstance();
    instance.addToken().addProperty("Word", "-Root-");
    for (List<String> row : rows) {
      instance.addToken().
        addProperty("Word", row.get(1)).
        addProperty("Index", row.get(0)).
        addProperty("Lemma", row.get(2)).
        addProperty("CPos", row.get(3)).
        addProperty("Pos", row.get(4)).
        addProperty("Feats", row.get(5));
    }
    for (List<String> row : rows) {
      //dependency
      int mod = Integer.parseInt(row.get(0));
      try {
        instance.addEdge(Integer.parseInt(row.get(6)), mod, row.get(7), "dep");
      } catch (Exception e) {
        System.err.println("Can't parse dependency");
        instance.getTokens().get(mod).addProperty("DepMissing", "missing");
      }
      //role
    }
    return instance;
  }

  /**
   * @see TabProcessor#createOpen(List<? extends List<String>>)
   */
  public NLPInstance createOpen(List<? extends List<String>> rows) {
   return null;
  }

  /**
   * @see TabProcessor#supportsOpen()
   */
  public boolean supportsOpen() {
    return false;
  }
}
