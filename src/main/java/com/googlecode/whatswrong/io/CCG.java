package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;
import java.util.Arrays;

/**
 * Loads CCG dependencies.
 *
 * @author Yonatan Bisk
 */
@SuppressWarnings({"MissingFieldJavaDoc"})
public class CCG implements TabProcessor {

    /**
     * The name of the processor.
     */
    public static final String name = "CCG";

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
        List<String> sentence = rows.get(0);
        // Skip <s> and dep count
        for (int i = 2; i < sentence.size(); ++i){
          String[] w_t_c = sentence.get(i).split("\\|");
          instance.addToken().
            addProperty("Word", w_t_c[0]).
            addProperty("Tag", w_t_c[1]).
            addProperty("Category", w_t_c[2]).
            addProperty("Index",String.valueOf(i-1));
        }
        //instance.addToken().addProperty("Word", "-Root-");
        int mod = 1;
        for (List<String> row : rows) {
            if (!row.get(0).equals("<s>") && !row.get(0).equals("<\\s>")) {
              //dependency
              try {
                  instance.addEdge(Integer.parseInt(row.get(1)), Integer.parseInt(row.get(0)), row.get(2) + "_" + row.get(3), "dep");
              } catch (Exception e) {
                  System.err.println("Can't parse dependency");
                  instance.getTokens().get(mod).addProperty("DepMissing", "missing");
              }
              ++mod;
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
