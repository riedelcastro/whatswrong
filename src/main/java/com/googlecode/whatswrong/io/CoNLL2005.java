package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;

/**
 * Loads CoNLL 2005 SRL data.
 *
 * @author Sebastian Riedel
 */

public class CoNLL2005 implements TabProcessor {


    /**
     * The name of the processor.
     */
    public static final String name = "CoNLL 2005";

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
            if (!row.get(9).equals("-")) {
                String sense = row.get(10) + "." + row.get(9);
                instance.addSpan(index, index, sense, "sense");
                TabFormat.extractSpan05(rows, 11 + predicateCount, "role", sense + ":", instance);
                ++predicateCount;
            }
            ++index;
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