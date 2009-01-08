package com.googlecode.whatswrong;

/**
 * An NLPInstanceFilter takes an NLPInstance and filters out edges, tokens, or token properties.
 *
 * @author Sebastian Riedel
 */
public interface NLPInstanceFilter {

    /**
     * Filter the given instance.
     *
     * @param original the original instance.
     * @return the filtered instance.
     */
    NLPInstance filter(NLPInstance original);
}
