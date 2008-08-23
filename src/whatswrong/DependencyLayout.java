package whatswrong;


import javautils.HashMultiMapList;
import javautils.Counter;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * A DependencyLayout lays out edges in a dependency parse layout. Here the edge
 * from head to modifier is represented as a directed edge that starts at the
 * head, first goes up and then down to the modifier. The height depends on the
 * number of other edges between the head and the modifier.
 *
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class DependencyLayout implements EdgeLayout {

  /**
   * Where do we start to draw
   */
  private int baseline = -1;
  /**
   * How many pixels to use per height level
   */
  private int heightPerLevel = 15;
  /**
   * The size of the arrow
   */
  private int arrowSize = 2;
  /**
   * How many extra pixels to start and end arrows from.
   */
  private int vertexExtraSpace = 12;
  /**
   * Should the edges be curved.
   */
  private boolean curve = true;

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
  private HashMap<Edge, Point> from;

  /**
   * A mapping from edges to their end points in the layout.
   */
  private HashMap<Edge, Point> to;

  /**
   * A mapping from edge shapes to the corresponding edge objects.
   */
  private HashMap<Shape, Edge> shapes = new HashMap<Shape, Edge>();

  /**
   * The set of selected edges.
   */
  private HashSet<Edge> selected = new HashSet<Edge>();

  /**
   * The set of visisible edges.
   */
  private HashSet<Edge> visible = new HashSet<Edge>();

  /**
   * The height of the layout.
   */
  private int maxHeight;

  /**
   * The width of the layout.
   */
  private int maxWidth;

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
   * Lays out the edges in a dependency style graph.
   *
   * @param edges       the edges to draw.
   * @param tokenLayout the layout of the tokens.
   * @param g2d         the graphics object to draw the layout to.
   *
   * @see EdgeLayout#layout(Collection<Edge>, TokenLayout, Graphics2D)
   */
  public void layout(Collection<Edge> edges, TokenLayout tokenLayout,
                     Graphics2D g2d) {

    if (visible.size() > 0) {
      edges = new HashSet<Edge>(edges);
      edges.retainAll(visible);
    }

    //find out height of each edge
    shapes.clear();

    HashMultiMapList<Token, Edge> loops = new HashMultiMapList<Token, Edge>();
    HashSet<Edge> allLoops = new HashSet<Edge>();
    HashSet<Token> tokens = new HashSet<Token>();
    for (Edge edge : edges) {
      tokens.add(edge.getFrom());
      tokens.add(edge.getTo());
      if (edge.getFrom() == edge.getTo()) {
        loops.add(edge.getFrom(), edge);
        allLoops.add(edge);
      }
    }
    edges.removeAll(allLoops);

    Counter<Edge> depth = new Counter<Edge>();
    Counter<Edge> offset = new Counter<Edge>();
    HashMultiMapList<Edge, Edge>
      dominates = new HashMultiMapList<Edge, Edge>();

    for (Edge over : edges)
      for (Edge under : edges) {
        if (over != under && (over.covers(under) || over.coversSemi(under) ||
          over.coversExactly(under) && over.lexicographicOrder(under) > 0)) {
          dominates.add(over, under);
        }
      }

    for (Edge edge : edges)
      calculateDepth(dominates, depth, edge);

    for (Edge left : edges)
      for (Edge right : edges) {
        if (left != right && left.crosses(right) &&
          depth.get(left).equals(depth.get(right))) {
          if (offset.get(left) == 0 && offset.get(right) == 0)
            offset.increment(left, heightPerLevel / 2);
          else if (offset.get(left).equals(offset.get(right))) {
            offset.put(left, heightPerLevel / 3);
            offset.put(right, heightPerLevel * 2 / 3);
          }
        }
      }

    //calculate maxHeight and maxWidth
    maxWidth = tokenLayout.getWidth();
    maxHeight = (depth.getMaximum() + 1) * heightPerLevel + 3;
    //in case there are no edges that cover other edges (depth == 0) we need
    //to increase the height slightly because loops on the same token
    //have height of 1.5 levels
    if (depth.getMaximum() == 0 && allLoops.size() > 0)
      maxHeight += heightPerLevel / 2;

    //build map from vertex to incoming/outgoing edges
    HashMultiMapList<Token, Edge> vertex2edges = new HashMultiMapList<Token, Edge>();
    for (Edge edge : edges) {
      vertex2edges.add(edge.getFrom(), edge);
      vertex2edges.add(edge.getTo(), edge);
    }
    //assign starting and end points of edges by sorting the edges per vertex
    from = new HashMap<Edge, Point>();
    to = new HashMap<Edge, Point>();
    for (final Token token : tokens) {
      List<Edge> connections = vertex2edges.get(token);
      Collections.sort(connections, new Comparator<Edge>() {
        /**
         * Compare to edges to see which one should be drawn higher
         *
         * @param edge1 of type Edge
         * @param edge2 of type Edge
         * @return int < 0 if edge1 < edge2 else >0.
         */
        public int compare(Edge edge1, Edge edge2) {
          //if they point in different directions order is defined by left to right
          if (edge1.leftOf(token) && edge2.rightOf(token)) return -1;
          if (edge2.leftOf(token) && edge1.rightOf(token)) return 1;
          //otherwise we order by length
          int diff = edge2.getLength() - edge1.getLength();
          if (edge1.leftOf(token) && edge2.leftOf(token)) {
            return diff != 0 ? -diff : edge1.lexicographicOrder(edge2);
          } else
            return diff != 0 ? diff : edge2.lexicographicOrder(edge1);
        }
      });
      //now put points along the token vertex wrt to ordering
      List<Edge> loopsOnVertex = loops.get(token);
      double width = (tokenLayout.getBounds(token).getWidth() + vertexExtraSpace) / (connections.size() + 1.0 + loopsOnVertex.size() * 2);
      double x = (tokenLayout.getBounds(token).getMinX() - (vertexExtraSpace / 2.0)) + width;
      for (Edge loop : loopsOnVertex) {
        Point point = new Point((int) x, baseline + maxHeight);
        from.put(loop, point);
        x += width;
      }
      for (Edge edge : connections) {
        Point point = new Point((int) x, baseline + maxHeight);
        if (edge.getFrom().equals(token))
          from.put(edge, point);
        else
          to.put(edge, point);
        x += width;

      }
      for (Edge loop : loopsOnVertex) {
        Point point = new Point((int) x, baseline + maxHeight);
        to.put(loop, point);
        x += width;
      }
    }

    //draw each edge
    edges.addAll(allLoops);
    for (Edge edge : edges) {
      //set Color and remember old color
      Color old = g2d.getColor();
      g2d.setColor(getColor(edge.getType()));
      //draw lines
      int height = baseline + maxHeight - (depth.get(edge) + 1) * heightPerLevel + offset.get(edge);
      if (edge.getFrom() == edge.getTo()) height -= heightPerLevel / 2;
      Point p1 = from.get(edge);
      if (p1 == null) System.out.println(edge);
      Point p2 = new Point(p1.x, height);
      Point p4 = to.get(edge);
      if (p4 == null) System.out.println(edges);
      Point p3 = new Point(p4.x, height);
      g2d.setStroke(getStroke(edge));
      //connection
      GeneralPath shape = curve ? createCurveArrow(p1, p2, p3, p4) : createRectArrow(p1, p2, p3, p4);
      g2d.draw(shape);
      g2d.drawLine(p4.x - arrowSize, p4.y - arrowSize, p4.x, p4.y);
      g2d.drawLine(p4.x + arrowSize, p4.y - arrowSize, p4.x, p4.y);

      //write label in the middle under
      Font font = new Font(g2d.getFont().getName(), Font.PLAIN, 8);
      FontRenderContext frc = g2d.getFontRenderContext();
      TextLayout layout = new TextLayout(edge.getLabel(), font, frc);
      int labelx = (int) (Math.min(p1.x, p3.x) + Math.abs(p1.x - p3.x) / 2 - layout.getBounds().getWidth() / 2);
      int labely = (int) (height + layout.getAscent()) + 1;
      layout.draw(g2d, labelx, labely);
      g2d.setColor(old);
      shapes.put(shape, edge);


    }


  }


  /**
   * Create an rectangular path that starts at p1 the goes to p2, p3 and finally
   * p4.
   *
   * @param p1 the first point
   * @param p2 the second point
   * @param p3 the third point
   * @param p4 the last point
   * @return an a path over the given points.
   */
  private GeneralPath createRectArrow(Point p1, Point p2, Point p3, Point p4) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(p1.x, p1.y);
    shape.lineTo(p2.x, p2.y);
    shape.lineTo(p3.x, p3.y);
    shape.lineTo(p4.x, p4.y);
    return shape;
  }

  /**
   * Create an curved path that starts at p1 and ends at p4. Points p2 and p3
   * are used as bezier control points.
   *
   * @param p1 the first point
   * @param p2 the second point
   * @param p3 the third point
   * @param p4 the last point
   * @return an a path over the given points.
   */
  private GeneralPath createCurveArrow(Point p1, Point p2, Point p3, Point p4) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(p1.x, p1.y);
    shape.curveTo(p2.x, p2.y, p2.x, p2.y, p2.x + (p3.x - p2.x) / 2, p2.y);
    shape.curveTo(p3.x, p3.y, p3.x, p3.y, p4.x, p4.y);
    shape.moveTo(p3.x, p3.y);
    shape.closePath();
    return shape;
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
  private int calculateDepth(HashMultiMapList<Edge, Edge> dominates,
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
   * @see EdgeLayout#getHeight()
   */
  public int getHeight() {
    return maxHeight;
  }

  /**
   * Return the width of the graph layout.
   *
   * @return the width of the graph.
   * @see EdgeLayout#getWidth()
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
   * The size of the arrow.
   *
   * @param arrowSize the size of the arrow.
   */
  public void setArrowSize(int arrowSize) {
    this.arrowSize = arrowSize;
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
   * Return the arrow size.
   *
   * @return the size of the arrow.
   */
  public int getArrowSize() {
    return arrowSize;
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
