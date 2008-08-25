package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;

/**
 * Loads CoNLL 2004 SRL data.
 *
 * @author Sebastian Riedel
 */

public class CoNLL2004 implements CoNLLProcessor {


  /**
   * The name of the processor.
   */
  public static final String name = "2004";

  /**
   * Returns the name of this processor.
   *
   * @return the name of this processor.
   */
  public String toString() {
    return name;
  }

  /**
   * @see CoNLLProcessor#create(List<? extends List<String>>)
   */
  public NLPInstance create(List<? extends List<String>> rows) {
    NLPInstance instance = new NLPInstance();
    int index = 0;
    for (List<String> row : rows) {
      instance.addToken().
        addProperty("Word", row.get(0)).
        addProperty("Index", String.valueOf(index));
      ++index;
    }
    int predicateCount = 0;
    index = 0;
    for (List<String> row : rows) {
      if (!row.get(1).equals("-")) {
        String sense = row.get(1);
        instance.addSpan(index, index, sense, "sense");
        CoNLLFormat.extractSpan05(rows, 2 + predicateCount, "role", sense + ":", instance);
        ++predicateCount;
      }
      ++index;
    }
    return instance;
  }

  /**
   * @see CoNLLProcessor#createOpen(List<? extends List<String>>)
   */
  public NLPInstance createOpen(List<? extends List<String>> rows) {
    return null;
  }

  /**
   * @see CoNLLProcessor#supportsOpen()
   */
  public boolean supportsOpen() {
    return false;
  }
}