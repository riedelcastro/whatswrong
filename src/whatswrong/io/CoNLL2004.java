package whatswrong.io;

import whatswrong.NLPInstance;

import java.util.List;

/**
 * @author Sebastian Riedel
 */

public class CoNLL2004 implements CoNLLProcessor {

  public static final String name = "2004";

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
      ++index;
    }
    int predicateCount = 0;
    index = 0;
    for (List<String> row : rows) {
      if (!row.get(1).equals("-")) {
        String sense = row.get(1);
        instance.addSpan(index, index, sense, "sense");
        CoNLLFormat.extractSpan05(rows, 2 + predicateCount, "role",sense + ":",instance);
        ++predicateCount;
      }
      ++index;
    }
    return instance;
  }

  public NLPInstance createOpen(List<? extends List<String>> rows) {
    return null;
  }

  public boolean supportsOpen() {
    return false;
  }
}