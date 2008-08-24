package com.googlecode.whatswrong.io;

import com.googlecode.whatswrong.NLPInstance;

import java.util.List;

/**
 * @author Sebastian Riedel
 */
 public interface CoNLLProcessor {
        NLPInstance create(List<? extends List<String>> rows);

        NLPInstance createOpen(List<? extends List<String>> rows);

        boolean supportsOpen();
    }