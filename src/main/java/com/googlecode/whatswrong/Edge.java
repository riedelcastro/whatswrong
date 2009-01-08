package com.googlecode.whatswrong;

/**
 * An Edge is a labelled and typed pair of tokens. It can represent dependencies
 * edges as well as spans. Along with a start and end (to and from) token an edge
 * has the following three attributes:
 * <ol>
 * <li>Type: The type of a edge denotes the type of information the
 * edge represents. For example, the type could be "dep" for edges that
 * represent syntactic dependencies, or "role" for edges that represent
 * semantic roles (a la CoNLL 2008).</li>
 * <li>Render Type: The render type of an edge controls how the edge will
 * be rendered. For example, both "dep" and "role" edges could have
 * the render type {@link com.googlecode.whatswrong.Edge.RenderType#dependency}</li>. Then
 * they are both drawn as directed edges in a dependency style graph.
 * <li>Label: This attribute classifies edges within a certain type. For example,
 * in the case of "dep" edges we could use the label "SUBJ" to denote
 * subject dependencies. </li>
 * </ol>
 *
 * @author Sebastian Riedel
 */
public class Edge {

  /**
   * The RenderType enum can be used to specify how the edge should be
   * rendered.
   */
  public static enum RenderType {
    /**
     * Draw edge as a span.
     */
    span,
    /**
     * Draw edge as a dependency
     */
    dependency
  }

  /**
   * The start token.
   */
  private Token from;
  /**
   * The end token.
   */
  private Token to;
  /**
   * The label of the edge.
   */
  private String label;
  /**
   * The type of the edge.
   */
  private String type;

  /**
   * How to render the edge
   */
  private RenderType renderType = RenderType.dependency;


  /**
   * Create new edge.
   *
   * @param from       from token.
   * @param to         to token
   * @param label      the label of the edge
   * @param type       the type of the edge (say, 'semantic role').
   * @param renderType the render type.
   */
  public Edge(Token from, Token to, String label,
              String type, RenderType renderType) {
    this.from = from;
    this.to = to;
    this.label = label;
    this.type = type;
    this.renderType = renderType;
  }

  /**
   * Creates a new edge with default render type (dependency).
   *
   * @param from  from token.
   * @param to    to token.
   * @param label the label of the edge.
   * @param type  the type of the edge.
   */
  public Edge(Token from, Token to, String label, String type) {
    this.from = from;
    this.to = to;
    this.label = label;
    this.type = type;
  }

  /**
   * If the type of label is qualified with a "qualifier:" prefix this method
   * returns "qualifier". Else it returns the complete type string.
   *
   * @return the prefix until ":" of the type string, or the complete type
   *         string if no ":" is contained in the string.
   */
  public String getTypePrefix() {
    int index = type.indexOf(':');
    return index == -1 ? type : type.substring(0, index);
  }

  /**
   * If the type of label is "prefix:postfix"  this method returns "postfix".
   * Else it returns the empty string.
   *
   * @return postfix after ":" or empty string if no ":" is contained in the
   *         type string.
   */
  public String getTypePostfix() {
    int index = type.indexOf(':');
    return index == -1 ? "" : type.substring(index + 1);
  }

  /**
   * Returns the mimimal index of both tokens in this edge.
   *
   * @return the mimimal index of both tokens in this edge.
   */
  public int getMinIndex() {
    return from.getIndex() < to.getIndex() ? from.getIndex() : to.getIndex();
  }

  /**
   * Returns the maximal index of both tokens in this edge.
   *
   * @return the maximal index of both tokens in this edge.
   */
  public int getMaxIndex() {
    return from.getIndex() > to.getIndex() ? from.getIndex() : to.getIndex();
  }

  /**
   * Returns the render type of this edge. For example, if this edge should be
   * drawn as span it would return {@link com.googlecode.whatswrong.Edge.RenderType#span}.
   *
   * @return the render type of this edge.
   */
  public RenderType getRenderType() {
    return renderType;
  }

  /**
   * Sets the render type of this edge. For example, if this edge should be
   * drawn as span it should be {@link com.googlecode.whatswrong.Edge.RenderType#span}.
   *
   * @param renderType the render type of this edge.
   */
  public void setRenderType(RenderType renderType) {
    this.renderType = renderType;
  }

  /**
   * Returns the start token of the edge.
   *
   * @return the start token of the edge.
   */
  public Token getFrom() {
    return from;
  }

  /**
   * Returns the end token of the edge.
   *
   * @return the end token of the edge.
   */
  public Token getTo() {
    return to;
  }

  /**
   * Returns the label of the edge. For example, for a dependency edge this
   * could be "SUBJ".
   *
   * @return the label of the edge.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns the type of the edge. This differs from the render type. For
   * example, we can represent semantic and syntactic dependencies both using
   * the dependency render type. However, the first one could have the edge type
   * "semantic" and the second one "syntactic".
   *
   * @return the type of the edge.
   */
  public String getType() {
    return type;
  }

  /**
   * Compares the type and label of this edge and the passed edge.
   *
   * @param edge the edge to compare to.
   * @return an integer indicating the lexicographic order of this edge and the
   *         given edge.
   */
  public int lexicographicOrder(Edge edge) {
    int result = type.compareTo(edge.type);
    return result == 0 ? -label.compareTo(edge.label) : -result;
  }

  /**
   * Checks whether the edge is to the left of the given token.
   *
   * @param token the token to compare to
   * @return true iff both tokens of this edge are to the left of the given
   *         token.
   */
  public boolean leftOf(Token token) {
    return from.getIndex() <= token.getIndex()
      && to.getIndex() <= token.getIndex();
  }

  /**
   * Checks whether the edge is to the right of the given token.
   *
   * @param token the token to compare to
   * @return true iff both tokens of this edge are to the right of the given
   *         token.
   */
  public boolean rightOf(Token token) {
    return from.getIndex() >= token.getIndex()
      && to.getIndex() >= token.getIndex();
  }

  /**
   * Returns the distance between the from and to token.
   *
   * @return the distance between the from and to token.
   */
  public int getLength() {
    return Math.abs(from.getIndex() - to.getIndex());
  }

  /**
   * Check whether this edge completely covers the specified edge.
   *
   * @param edge the edge to check whether it is covered by this edge.
   * @return true iff the given edge is completely covered by this edge.
   */
  public boolean covers(Edge edge) {
    return getMinIndex() < edge.getMinIndex()
      && getMaxIndex() > edge.getMaxIndex();
  }

  /**
   * Check whether this edge spans the same sequence of tokens as the given
   * edge.
   *
   * @param edge the edge to compare with.
   * @return true iff this edge covers the same sequence of tokens as the given
   *         edge.
   */
  public boolean coversExactly(Edge edge) {
    return getMinIndex() == edge.getMinIndex()
      && getMaxIndex() == edge.getMaxIndex();
  }

  /**
   * Checks whether this edge covers the given edge and is aligned with it on
   * one side.
   *
   * @param edge the edge to compare with.
   * @return true iff this edge covers the given edge and exactly one of their
   *         tokens are equal.
   */
  public boolean coversSemi(Edge edge) {
    return
      getMinIndex() < edge.getMinIndex() && getMaxIndex() == edge.getMaxIndex() ||
        getMinIndex() == edge.getMinIndex() && getMaxIndex() > edge.getMaxIndex();
  }

  /**
   * Checks whether this edge overlaps the given edge.
   *
   * @param edge the edge to compare with.
   * @return true iff the edges overlapn.
   */
  public boolean overlaps(Edge edge) {
    return getMinIndex() <= edge.getMinIndex() &&
      getMaxIndex() <= edge.getMaxIndex() &&
      getMaxIndex() >= edge.getMinIndex() ||
      getMinIndex() >= edge.getMinIndex() &&
        getMaxIndex() <= edge.getMaxIndex() &&
        getMaxIndex() <= edge.getMinIndex();
  }


  /**
   * Checks whether the given edge is covered by this edge and at least one
   * token is not aligned.
   *
   * @param edge the edge to compare with.
   * @return true if this edge covers the given edge and at least one token is
   *         not aligned.
   */
  public boolean strictlyCovers(Edge edge) {
    return getMinIndex() < edge.getMinIndex() && getMaxIndex() >= edge.getMaxIndex() ||
      getMinIndex() <= edge.getMinIndex() && getMaxIndex() > edge.getMaxIndex();
  }


  /**
   * Returns a string representation of this edge.
   *
   * @return a string representation of this edge that shows label, type and the
   *         indices of the start and end tokens.
   */
  public String toString() {
    return from.getIndex() + "-" + label + "->" + to.getIndex() + "(" + type + ")";
  }

  /**
   * Checks whether the given edge crosses this edge.
   *
   * @param edge the edge to compare to.
   * @return true iff this edge crosses the given edge.
   */
  public boolean crosses(Edge edge) {
    return getMinIndex() > edge.getMinIndex()
      && getMinIndex() < edge.getMaxIndex()
      && getMaxIndex() > edge.getMaxIndex() ||
      edge.getMinIndex() > getMinIndex()
        && edge.getMinIndex() < getMaxIndex()
        && edge.getMaxIndex() > getMaxIndex();

  }


  /**
   * Checks whether to edges are equal
   *
   * @param o the other edge
   * @return true if both edges have the same type, label and the same from and
   *         to tokens.
   */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Edge that = (Edge) o;

    if (from != null ? !from.equals(that.from) : that.from != null)
      return false;
    if (label != null ? !label.equals(that.label) : that.label != null)
      return false;
    if (to != null ? !to.equals(that.to) : that.to != null) return false;
    //noinspection RedundantIfStatement
    if (type != null ? !type.equals(that.type) : that.type != null)
      return false;

    return true;
  }

  /**
   * Returns a hashcode based on type, label, from and to token.
   *
   * @return a hashcode based on type, label, from and to token.
   */
  public int hashCode() {
    int result;
    result = (from != null ? from.hashCode() : 0);
    result = 31 * result + (to != null ? to.hashCode() : 0);
    result = 31 * result + (label != null ? label.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
}
