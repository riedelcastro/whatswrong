package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;

/**
 * Loads Malt-TAB dependencies.
 *
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MissingFieldJavaDoc"})
public class MaltTab implements TabProcessor {

  /**
   * The name of the processor.
   */
  public static final String name = "Malt-TAB";

  /**
   * Returns the name of this processor.
   *
   * @return the name of this processor.
   */
  public String toString() {
    return name;
  }

  /**
   * @see com.googlecode.whatswrong.io.TabProcessor#create(java.util.List<? extends java.util.List<String>>)
   */
  public NLPInstance create(List<? extends List<String>> rows) {

    NLPInstance instance = new NLPInstance();
    instance.addToken().addProperty("Word", "-Root-");
    int index = 1;
    for (List<String> row : rows) {
      instance.addToken().
        addProperty("Word", row.get(0)).
        addProperty("Index", String.valueOf(index++)).
        addProperty("Pos", row.get(1));
    }
    int mod = 1;
    for (List<String> row : rows) {
      //dependency
      try {
        instance.addEdge(Integer.parseInt(row.get(2)), mod, row.get(3), "dep");
      } catch (Exception e) {
        System.err.println("Can't parse dependency");
        instance.getTokens().get(mod).addProperty("DepMissing", "missing");
      }
      //role
      ++mod;
    }
    return instance;
  }

  /**
   * @see com.googlecode.whatswrong.io.TabProcessor#createOpen(java.util.List<? extends java.util.List<String>>)
   */
  public NLPInstance createOpen(List<? extends List<String>> rows) {
   return null;
  }

  /**
   * @see com.googlecode.whatswrong.io.TabProcessor#supportsOpen()
   */
  public boolean supportsOpen() {
    return false;
  }
}