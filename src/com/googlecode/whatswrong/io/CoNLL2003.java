package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;

/**
 * Loads CoNLL 2003 chunk and NER data.
 *
 * @author Sebastian Riedel
 */

public class CoNLL2003 implements CoNLLProcessor {

  /**
   * The name of the processor.
   */
  public static final String name = "2003";

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

      instance.addSpan(index, index, row.get(1), "pos");
      instance.addSpan(index, index, row.get(2), "chunk (BIO)");
      instance.addSpan(index, index, row.get(3), "ner (BIO)");
      ++index;
    }

    CoNLLFormat.extractSpan03(rows, 2, "chunk", instance);
    CoNLLFormat.extractSpan03(rows, 3, "ner", instance);

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