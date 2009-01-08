/**
 * The whatswrong package is the top-level package of What's Wrong With My NLP.
 * It contains classes to represent NLP utterances as well as classes to
 * display and compare them. The main application class is
 * {@link com.googlecode.whatswrong.WhatsWrongWithMyNLP} while
 * the most important rendering class is likely {@link com.googlecode.whatswrong.NLPCanvas},
 * which draws graphical representations of {@link com.googlecode.whatswrong.NLPInstance}
 * objects. Which instance of a corpus is chosen to be rendered is controlled
 * by a {@link com.googlecode.whatswrong.CorpusNavigator}. The
 * {@link com.googlecode.whatswrong.CorpusLoader}
 * objects allow the user to pick one or two (for diff operation) corpora
 * from a list of loaded corpora.
 *
 * @see com.googlecode.whatswrong.WhatsWrongWithMyNLP
 * @see com.googlecode.whatswrong.NLPCanvas
 * @see com.googlecode.whatswrong.NLPInstance
 * @see com.googlecode.whatswrong.CorpusNavigator
 * @see com.googlecode.whatswrong.CorpusLoader
 */
package com.googlecode.whatswrong;