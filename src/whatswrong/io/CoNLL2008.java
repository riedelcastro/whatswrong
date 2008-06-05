package whatswrong.io;

import whatswrong.TokenProperty;
import whatswrong.NLPInstance;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Sebastian Riedel
 */

public class CoNLL2008 implements CoNLLProcessor {

  public static final String name = "2008";

  private TokenProperty
    ne = new TokenProperty("Named Entity", 10),
    bbn = new TokenProperty("Named Entity BBN", 11),
    wn = new TokenProperty("WordNet", 11);

  public String toString() {
    return name;
  }

  public NLPInstance create(List<? extends List<String>> rows) {
    NLPInstance instance = new NLPInstance();
    instance.addToken().addProperty("Word", "-Root-");
    ArrayList<Integer> predicates = new ArrayList<Integer>();
    for (List<String> row : rows) {
      instance.addToken().
        addProperty("Word", row.get(1)).
        addProperty("Index", row.get(0)).
        addProperty("Lemma", row.get(2)).
        addProperty("PoS", row.get(3)).
        addProperty("Split Form", row.get(5)).
        addProperty("Split Lemma", row.get(6)).
        addProperty("Split PoS", row.get(7));
      if (!row.get(10).equals("_")){
        int index = Integer.parseInt(row.get(0));
        predicates.add(index);
        instance.addSpan(index,index,row.get(10),"sense");
      }
    }
    for (List<String> row : rows) {
      //dependency
      if (!row.get(8).equals("_"))
        instance.addEdge(Integer.parseInt(row.get(8)), Integer.parseInt(row.get(0)), row.get(9), "dep");
      //role
      for (int col = 11; col < row.size(); ++col) {
        String label = row.get(col);
        if (!label.equals("_")) {
          Integer pred = predicates.get(col - 11);
          int arg = Integer.parseInt(row.get(0));
          //if (arg != pred)
          instance.addEdge(pred, arg, label, "role");
        }
      }
    }
    return instance;
  }

  public NLPInstance createOpen(List<? extends List<String>> rows) {
    NLPInstance instance = new NLPInstance();
    instance.addToken();
    for (List<String> row : rows) {
      instance.addToken().
        addProperty(ne, row.get(0)).
        addProperty(bbn, row.get(1)).
        addProperty(wn, row.get(2));
    }
    int index = 1;
    for (List<String> row : rows) {
      //dependency
      instance.addEdge(Integer.parseInt(row.get(3)), index++, row.get(4), "malt");
    }
    return instance;
  }

  public boolean supportsOpen() {
    return true;
  }
}