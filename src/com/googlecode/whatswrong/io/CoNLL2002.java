package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;

/**
 * @author Sebastian Riedel
 */

public class CoNLL2002 implements CoNLLProcessor {

  public static final String name = "2002";


  public String toString() {
    return name;
  }

  public NLPInstance create(List<? extends List<String>> rows) {

    NLPInstance instance = new NLPInstance();
    int index = 0;
    for (List<String> row : rows) {
      instance.addToken().
        addProperty("Word", row.get(0)).
        addProperty("Index", String.valueOf(index));
      instance.addSpan(index, index, row.get(1), "ner (BIO)");
      ++index;
    }

    CoNLLFormat.extractSpan00(rows, 1, "ner", instance);

    return instance;
  }


  public NLPInstance createOpen(List<? extends List<String>> rows) {
    return null;
  }

  public boolean supportsOpen() {
    return false;
  }
}