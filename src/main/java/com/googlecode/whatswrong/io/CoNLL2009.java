package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;
import com.googlecode.whatswrong.TokenProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads CoNLL 2008 Joint SRL and Dependency data.
 *
 * @author Sebastian Riedel
 */

@SuppressWarnings({"MissingFieldJavaDoc"})
public class CoNLL2009 implements TabProcessor {

    /**
     * The name of the processor.
     */
    public static final String name = "CoNLL 2009";

    private TokenProperty
        ne = new TokenProperty("Named Entity", 10),
        bbn = new TokenProperty("Named Entity BBN", 11),
        wn = new TokenProperty("WordNet", 12);

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
        ArrayList<Integer> predicates = new ArrayList<Integer>();
        for (List<String> row : rows) {
            instance.addToken().
                addProperty("Word", row.get(1)).
                addProperty("Index", row.get(0)).
                addProperty("Lemma", row.get(2)).
                addProperty("PLemma", row.get(3)).
                addProperty("PoS", row.get(4)).
                addProperty("PPoS", row.get(5)).
                addProperty("Feat", row.get(6)).
                addProperty("PFeat", row.get(7));
            if (!row.get(13).equals("_")) {
                int index = Integer.parseInt(row.get(0));
                predicates.add(index);
                instance.addSpan(index, index, row.get(13), "sense");
            }
        }
        for (List<String> row : rows) {
            //dependency
            if (!row.get(8).equals("_"))
                instance.addEdge(Integer.parseInt(row.get(8)), Integer.parseInt(row.get(0)), row.get(10), "dep");
            if (!row.get(9).equals("_"))
                instance.addEdge(Integer.parseInt(row.get(9)), Integer.parseInt(row.get(0)), row.get(11), "pdep");
            //role
            for (int col = 14; col < row.size(); ++col) {
                String label = row.get(col);
                if (!label.equals("_")) {
                    Integer pred = predicates.get(col - 14);
                    int arg = Integer.parseInt(row.get(0));
                    //if (arg != pred)
                    instance.addEdge(pred, arg, label, "role");
                }
            }
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