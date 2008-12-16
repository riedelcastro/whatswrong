package com.googlecode.whatswrong;

import com.googlecode.whatswrong.javautils.HashMultiMapLinkedList;
import com.googlecode.whatswrong.javautils.Counter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Set;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * An AbstractEdgeLayout serves as a base class for edge layout classes. It
 * mostly stores properties associated with drawing edge layouts, such as
 * whether lines should be curved or not.
 *
 * @author Sebastian Riedel
 */
public abstract class AbstractEdgeLayout  {
  /**
   * Where do we start to draw
   */
  protected int baseline = -1;
  /**
   * How many pixels to use per height level
   */
  protected int heightPerLevel = 15;
  /**
   * How many extra pixels to start and end arrows from.
   */
  protected int vertexExtraSpace = 12;
  /**
   * Should the edges be curved.
   */
  protected boolean curve = true;
  /**
   * A mapping from string to colors. If an edge has a type that matches one of
   * the key strings it will get the corresponding color.
   */
  private HashMap<String, Color> colors = new HashMap<String, Color>();
  /**
   * A mapping from string to strokes. If an edge has a type that matches one of
   * the key strings it will get the corresponding stroke.
   */
  private HashMap<String, BasicStroke>
    strokes = new HashMap<String, BasicStroke>();
  /**
   * The stroke to use as default.
   */
  private BasicStroke defaultStroke = new BasicStroke();
  /**
   * A mapping from edges to their start points in the layout.
   */
  protected HashMap<Edge, Point> from;
  /**
   * A mapping from edges to their end points in the layout.
   */
  protected HashMap<Edge, Point> to;
  /**
   * A mapping from edge shapes to the corresponding edge objects.
   */
  protected HashMap<Shape, Edge> shapes = new HashMap<Shape, Edge>();
  /**
   * The set of selected edges.
   */
  private HashSet<Edge> selected = new HashSet<Edge>();
  /**
   * The set of visisible edges.
   */
  protected HashSet<Edge> visible = new HashSet<Edge>();
  /**
   * The height of the layout. This property is to be set by the {@link
   * com.googlecode.whatswrong.EdgeLayout#layout(java.util.Collection, TokenLayout,
   * java.awt.Graphics2D)} method after the layout process.
   */
  protected int maxHeight;
  /**
   * The width of the layout. This property is to be set by the {@link
   * com.googlecode.whatswrong.EdgeLayout#layout(java.util.Collection, TokenLayout,
   * java.awt.Graphics2D)} method after the layout process.
   */
  protected int maxWidth;

  /**
   * Set the color for edges of a certain type.
   *
   * @param type  the type of the edges we want to change the color for.
   * @param color the color of the edges of the given type.
   */
  public void setColor(String type, Color color) {
    colors.put(type, color);
  }

  /**
   * Set the stroke type for edges of a certain type.
   *
   * @param type   the type to change the stroke for.
   * @param stroke the stroke for edges of the given type.
   */
  public void setStroke(String type, BasicStroke stroke) {
    strokes.put(type, stroke);
  }

  /**
   * Get the stroke for a given edge.
   *
   * @param edge the edge we need the stroke for.
   * @return the stroke for the edge.
   */
  public BasicStroke getStroke(Edge edge) {
    BasicStroke stroke = getStroke(edge.getType());
    return (selected.contains(edge)) ?
      new BasicStroke(stroke.getLineWidth() + 1.5f, stroke.getEndCap(), stroke.getLineJoin(),
        stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase()) :
      stroke;
  }

  /**
   * Returns the stroke for a given type.
   *
   * @param type the type of edges to get the stroke for.
   * @return the stroke for the given type.
   */
  public BasicStroke getStroke(String type) {
    for (String substring : strokes.keySet())
      if (type.contains(substring)) {
        return strokes.get(substring);
      }
    return defaultStroke;
  }

  /**
   * Return the color for edges of the given type.
   *
   * @param type the type for which we want the color for.
   * @return the color for the given edge type.
   */
  public Color getColor(String type) {
    for (String substring : colors.keySet())
      if (type.contains(substring)) return colors.get(substring);
    return Color.BLACK;
  }

  /**
   * Add an edge to the selection. Selected edges will be drawn using a bolder
   * stroke.
   *
   * @param edge the edge to add to the selection.
   */
  public void addToSelection(Edge edge) {
    selected.add(edge);
  }

  /**
   * Remove an edge from the selection.
   *
   * @param edge the edge to remove.
   */
  public void removeFromSelection(Edge edge) {
    selected.remove(edge);
  }

  /**
   * Remove all edges from the selection.
   */
  public void clearSelection() {
    selected.clear();
  }

  /**
   * Show only the given edges.
   *
   * @param edges the edges to show.
   */
  public void onlyShow(Collection<Edge> edges) {
    this.visible.clear();
    this.visible.addAll(edges);
  }

  /**
   * Show all edges.
   */
  public void showAll() {
    visible.clear();
  }

  /**
   * Change whether the given edge is selected or not.
   *
   * @param edge the edge to add or remove from the selection.
   */
  public void toggleSelection(Edge edge) {
    if (selected.contains(edge)) selected.remove(edge);
    else selected.add(edge);
  }

  /**
   * Returns the set of selected edges.
   *
   * @return the set of selected edges.
   */
  public Set<Edge> getSelected() {
    return Collections.unmodifiableSet(selected);
  }

  /**
   * Select only one edge and remove all other edges from the selection.
   *
   * @param edge the edge to select.
   */
  public void select(Edge edge) {
    selected.clear();
    selected.add(edge);
  }

  /**
   * Get the Edge at a given location.
   *
   * @param p      the location of the edge.
   * @param radius the radius around the point which the edge should cross.
   * @return the edge that crosses circle around the given point with the given
   *         radius.
   */
  public Edge getEdgeAt(Point2D p, int radius) {
    Rectangle2D cursor = new Rectangle.Double(p.getX() - radius / 2, p.getY() - radius / 2, radius, radius);
    double maxY = Integer.MIN_VALUE;
    Edge result = null;
    for (Shape s : shapes.keySet()) {
      if (s.intersects(cursor) && s.getBounds().getY() > maxY) {
        result = shapes.get(s);
        maxY = s.getBounds().getY();
      }
    }
    return result;
  }

  /**
   * Calculate the number of edges under each edge and returns the max. of these
   * numbers.
   *
   * @param dominates a map from edges to the edges it dominates.
   * @param depth     the resulting depths of each edge.
   * @param root      the root of the graph.
   * @return the max. depth.
   */
  protected int calculateDepth(HashMultiMapLinkedList<Edge, Edge> dominates,
                               Counter<Edge> depth,
                               Edge root) {
    if (depth.get(root) > 0) return depth.get(root);
    if (dominates.get(root).size() == 0) {
      return 0;
    }
    int max = 0;
    for (Edge children : dominates.get(root)) {
      int current = calculateDepth(dominates, depth, children);
      if (current > max) max = current;
    }
    depth.put(root, max + 1);
    return max + 1;

  }

  /**
   * Return the point at the start of the given edge.
   *
   * @param edge the edge to get the starting point from.
   * @return the start point of the given edge.
   */
  public Point getFrom(Edge edge) {
    return from.get(edge);
  }

  /**
   * Return the point at the end of the given edge.
   *
   * @param edge the edge to get the end point from.
   * @return the end point of the given edge.
   */
  public Point getTo(Edge edge) {
    return to.get(edge);
  }

  /**
   * Return the height of the graph layout.
   *
   * @return the height of the graph.
   * @see com.googlecode.whatswrong.EdgeLayout#getHeight()
   */
  public int getHeight() {
    return maxHeight;
  }

  /**
   * Return the width of the graph layout.
   *
   * @return the width of the graph.
   * @see com.googlecode.whatswrong.EdgeLayout#getWidth()
   */
  public int getWidth() {
    return maxWidth;
  }

  /**
   * The number of pixels per graph layer.
   *
   * @return the number of pixels per graph layer.
   */
  public int getHeightPerLevel() {
    return heightPerLevel;
  }

  /**
   * Should edges be curved
   *
   * @return true iff graph is curved.
   */
  public boolean isCurve() {
    return curve;
  }

  /**
   * Should edges be curved
   *
   * @param curve true iff if graph should be curved.
   */
  public void setCurve(boolean curve) {
    this.curve = curve;
  }

  /**
   * At how many pixels from the bottom should the graph start.
   *
   * @param baseline how many pixels from the bottom should the graph start.
   */
  public void setBaseline(int baseline) {
    this.baseline = baseline;
  }

  /**
   * Sets the number of pixels for each graph layer.
   *
   * @param heightPerLevel number of pixels for each graph layer.
   */
  public void setHeightPerLevel(int heightPerLevel) {
    this.heightPerLevel = heightPerLevel;
  }

  /**
   * The extra number of pixels around a token vertex we can use for starting
   * and end points of edges.
   *
   * @param vertexExtraSpace The extra number of pixels around a token vertex we
   *                         can use for starting and end points of edges.
   */
  public void setVertexExtraSpace(int vertexExtraSpace) {
    this.vertexExtraSpace = vertexExtraSpace;
  }

  /**
   * The extra number of pixels around a token vertex we can use for starting
   * and end points of edges.
   *
   * @return The extra number of pixels around a token vertex we can use for
   *         starting and end points of edges.
   */
  public int getVertexExtraSpace() {
    return vertexExtraSpace;
  }

  /**
   * The number of pixels below the graph (between the tokens and the edges).
   *
   * @return the baseline size.
   */
  public int getBaseline() {
    return baseline;
  }
}
