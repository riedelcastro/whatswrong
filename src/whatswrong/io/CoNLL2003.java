package whatswrong.io;

import whatswrong.NLPInstance;

import java.util.List;

/**
 * @author Sebastian Riedel
 */

public class CoNLL2003 implements CoNLLProcessor {

  public static final String name = "2003";


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

      instance.addSpan(index, index, row.get(1), "pos");
      instance.addSpan(index, index, row.get(2), "chunk (BIO)");
      instance.addSpan(index, index, row.get(3), "ner (BIO)");
      ++index;
    }

    CoNLLFormat.extractSpan03(rows, 2, "chunk", instance);
    CoNLLFormat.extractSpan03(rows, 3, "ner", instance);

    return instance;
  }


  public NLPInstance createOpen(List<? extends List<String>> rows) {
    return null;
  }

  public boolean supportsOpen() {
    return false;
  }
}