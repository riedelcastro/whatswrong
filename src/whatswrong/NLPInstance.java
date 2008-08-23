package whatswrong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * An NLPInstance represents a sentence or any other kind of utterance and some
 * of its (NLP) properties. Properties of sentence are its tokens, that have
 * their own properties, and edges between tokens. Such edges can represent
 * syntactic or semantic dependencies, such as SRL predicate-argument relations,
 * as well as annotated spans (such as NP chunks or NER entities).
 *
 * @author Sebastian Riedel
 */
public class NLPInstance {

  /**
   * Contains the edges of this instance.
   */
  private List<Edge> edges = new ArrayList<Edge>();
  /**
   * Contains the tokens of this instance.
   */
  private List<Token> tokens = new ArrayList<Token>();
  /**
   * Contains a mapping from sentence indices to tokens.
   */
  private HashMap<Integer, Token> map = new HashMap<Integer, Token>();

  /**
   * Creates an empty NLPInstance without edges or tokens.
   */
  public NLPInstance() {
  }

  /**
   * Creates a new NLPInstance with the given tokens and edges. The passed
   * collections will be copied and not changed.
   *
   * @param tokens the tokens of the sentence.
   * @param edges  the edges of the sentence.
   */
  public NLPInstance(final Collection<Token> tokens,
                     final Collection<Edge> edges) {
    this.tokens.addAll(tokens);
    for (Token t : tokens) map.put(t.getIndex(), t);
    this.edges.addAll(edges);
  }

  /**
   * Creates and adds an edge from the token at the given 'from' index to the
   * token at the given 'to' index with the given label and type. The edge will
   * have the default render type.
   *
   * @param from  index of the token the edge should start at. The token at the
   *              given index must already exist in the sentence.
   * @param to    index of the token edge should end at. The token at the given
   *              index must already exist in the sentence.
   * @param label the label of the edge.
   * @param type  the type of edge.
   * @see whatswrong.Edge
   */
  public void addEdge(final int from, final int to,
                      final String label, final String type) {
    edges.add(new Edge(map.get(from), map.get(to), label, type));
  }


  /**
   * Creates and adds a new edge with the given properties.
   *
   * @param from       index of the token the edge should start at. The token at
   *                   the given index must already exist in the sentence.
   * @param to         index of the token edge should end at. The token at the
   *                   given index must already exist in the sentence.
   * @param label      the label of the edge.
   * @param type       the type of edge.
   * @param renderType the render type of the edge.
   * @see whatswrong.Edge
   */
  public void addEdge(final int from, final int to, final String label,
                      final String type, final Edge.RenderType renderType) {
    edges.add(new Edge(map.get(from), map.get(to), label, type, renderType));
  }

  /**
   * Creates and adds an edge with rendertype {@link whatswrong.Edge.RenderType#span}
   *
   * @param from  index of the token the edge should start at. The token at the
   *              given index must already exist in the sentence.
   * @param to    index of the token edge should end at. The token at the given
   *              index must already exist in the sentence.
   * @param label the label of the edge.
   * @param type  the type of edge.
   * @see whatswrong.Edge
   */
  public void addSpan(final int from, final int to,
                      final String label, final String type) {
    edges.add(new Edge(map.get(from), map.get(to), label, type,
      Edge.RenderType.span));
  }

  /**
   * Creates and adds an edge with rendertype {@link whatswrong.Edge.RenderType#dependency}
   *
   * @param from  index of the token the edge should start at. The token at the
   *              given index must already exist in the sentence.
   * @param to    index of the token edge should end at. The token at the given
   *              index must already exist in the sentence.
   * @param label the label of the edge.
   * @param type  the type of edge.
   * @see whatswrong.Edge
   */
  public void addDependency(final int from, final int to,
                            final String label, final String type) {
    edges.add(new Edge(map.get(from), map.get(to), label, type,
      Edge.RenderType.dependency));
  }

  /**
   * Creates and adds an edge with the given properties. It will have the
   * default render type.
   *
   * @param from  The start token. The created edge will start at the token of
   *              this sentence with the same index as the provided token. This
   *              means the start token of created edge does not need to be
   *              equal to the provided token -- they just have to have the same
   *              index.
   * @param to    the end token. The created edge will end at the token of this
   *              sentence with the same index as the provided token. This means
   *              that the end token of created edge does not need to be equal
   *              to the provided token -- they just have to have the same
   *              index.
   * @param label the label of the edge.
   * @param type  the type of edge.
   * @see whatswrong.Edge
   */
  public void addEdge(final Token from, final Token to,
                      final String label, final String type) {
    addEdge(from.getIndex(), to.getIndex(), label, type);
  }

  /**
   * Creates and adds an edge with the given properties. It will have the
   * default render type.
   *
   * @param from       The start token. The created edge will start at the token
   *                   of this sentence with the same index as the provided
   *                   token. This means the start token of created edge does
   *                   not need to be equal to the provided token -- they just
   *                   have to have the same index.
   * @param to         the end token. The created edge will end at the token of
   *                   this sentence with the same index as the provided token.
   *                   This means that the end token of created edge does not
   *                   need to be equal to the provided token -- they just have
   *                   to have the same index.
   * @param label      the label of the edge.
   * @param type       the type of edge.
   * @param renderType the render type of the edge.
   * @see whatswrong.Edge
   */
  public void addEdge(final Token from, final Token to, final String label,
                      final String type, Edge.RenderType renderType) {
    addEdge(from.getIndex(), to.getIndex(), label, type, renderType);
  }

  /**
   * Adds the given collection of tokens to this instance.
   *
   * @param tokens the tokens to add.
   */
  public void addTokens(final Collection<Token> tokens) {
    this.tokens.addAll(tokens);
    for (Token t : tokens) map.put(t.getIndex(), t);
  }

  /**
   * Adds the given edges to this instance.
   *
   * @param edges the edges to add.
   */
  public void addEdges(final Collection<Edge> edges) {
    this.edges.addAll(edges);
  }

  /**
   * Merges the given instance with this instance. A merge will add for every
   * token i all properties of the token i of the passed instance
   * <code>nlp</code>. It will also add every edge between i and i in the given
   * instance <code>nlp</code> as an edge between the tokens i and j of this
   * instance, using the same type, label and rendertype as the original edge.
   *
   * @param nlp the instance to merge into this instance.
   */
  public void merge(final NLPInstance nlp) {
    for (int i = 0; i < Math.min(tokens.size(), nlp.tokens.size()); ++i) {
      tokens.get(i).merge(nlp.tokens.get(i));
    }
    for (Edge edge : nlp.edges) {
      addEdge(edge.getFrom().getIndex(), edge.getTo().getIndex(),
        edge.getLabel(), edge.getType(), edge.getRenderType());
    }
  }

  /**
   * Adds token that has the provided properties with default property names.
   *
   * @param properties an vararray of strings.
   */
  public void addTokenWithProperties(final String... properties) {
    Token token = new Token(tokens.size());
    for (String property : properties) token.addProperty(property);
    tokens.add(token);
    map.put(token.getIndex(), token);
  }

  /**
   * Adds a new token and returns it.
   *
   * @return the token that was added.
   */
  public Token addToken() {
    Token vertex = new Token(tokens.size());
    tokens.add(vertex);
    map.put(vertex.getIndex(), vertex);
    return vertex;
  }

  /**
   * Adds a token at a certain index. This method can be used when we don't want
   * to build the sentence in order. Note that if you build the instance using
   * this method you have to call {@link NLPInstance#consistify()} when you are
   * done.
   *
   * @param index the index of the token to add.
   * @return the token that was added.
   */
  public Token addToken(final int index) {

    Token vertex = map.get(index);
    if (vertex == null) {
      vertex = new Token(index);
      map.put(index, vertex);
    }
    return vertex;
  }

  /**
   * If tokesn were added with {@link whatswrong.NLPInstance#addToken(int)} this
   * method ensures that all internal representations of the token sequence are
   * consistent.
   */
  public void consistify() {
    tokens.addAll(map.values());
    Collections.sort(tokens);
  }


  /**
   * Returns all edges of this instance.
   *
   * @return all edges of this instance as unmodifiable list.
   */
  public List<Edge> getEdges() {
    return Collections.unmodifiableList(edges);
  }

  /**
   * Returns all edges of this instance with the given render type.
   *
   * @param renderType the render type of the edges to return.
   * @return all edges of this instance with the given render type. This list
   *         can be altered if needed.
   */
  public List<Edge> getEdges(final Edge.RenderType renderType) {
    ArrayList<Edge> result = new ArrayList<Edge>(edges.size());
    for (Edge e : edges) if (e.getRenderType() == renderType) result.add(e);
    return result;
  }

  /**
   * Returns the token at the given index.
   *
   * @param index the index of the token to return
   * @return the token at the given index.
   */
  public Token getToken(final int index) {
    return map.get(index);
  }

  /**
   * Returns a list of all tokens in this instance.
   *
   * @return an unmodifiable list of all tokens of this sentence, in the right
   *         order.
   */
  public List<Token> getTokens() {
    return Collections.unmodifiableList(tokens);
  }

  /**
   * Returns a string representation of this instance. Mostly for debugging
   * purposes.
   *
   * @return a string representation of this instance.
   */
  public String toString() {
    return tokens + "\n" + map + "\n" + edges;
  }
}
