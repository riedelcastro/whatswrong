package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;

/**
 * A TabProcessor takes a table of string values and returns an NLPInstance. This table of string value corresponds to
 * the standard way of representing sentences in the CoNLL shared tasks as well as the MALT-Tab format for
 * dependencies.
 *
 * @author Sebastian Riedel
 */
public interface TabProcessor {
    /**
     * Create an NLPInstance from the given table (list of rows) of strings.
     *
     * @param rows the rows that represent the column separated values in Tab format files.
     * @return an NLPInstance that represents the given rows.
     */
    NLPInstance create(List<? extends List<String>> rows);

    /**
     * Create an NLPInstance from the given table (list of rows) of strings, assuming that the passed rows are from the
     * open dataset.
     *
     * @param rows the rows that represent the column separated values in Tab format files.
     * @return an NLPInstance that represents the given rows.
     */
    NLPInstance createOpen(List<? extends List<String>> rows);

    /**
     * Does this processor support loading of open datasets (as in "CoNLL Open Track").
     *
     * @return true iff the processor supports loading of open datasets.
     */
    boolean supportsOpen();
}