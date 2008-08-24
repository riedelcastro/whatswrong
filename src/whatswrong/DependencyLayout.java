package whatswrong;


import javautils.Counter;
import javautils.HashMultiMapLinkedList;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A DependencyLayout lays out edges in a dependency parse layout. Here the edge
 * from head to modifier is represented as a directed edge that starts at the
 * head, first goes up and then down to the modifier. The height depends on the
 * number of other edges between the head and the modifier.
 *
 * <p>Note that all incoming and outgoing edges of a token are placed along the
 * upper edge of the token bounding box in an order that depends on the distance
 * of the other token of the edge. The further away the other token is, the
 * closer the edge start or end point is to the middle of the token bounding
 * box. There is one exception to this rule: self loops always start at the
 * leftmost position and end at the rightmost position.
 *
 * @author Sebastian Riedel
 */
public class DependencyLayout extends AbstractEdgeLayout {

  /**
   * The size of the arrow
   */
  private int arrowSize = 2;


  /**
   * Lays out the edges in a dependency style graph.
   *
   * @param edges       the edges to draw.
   * @param tokenLayout the layout of the tokens.
   * @param g2d         the graphics object to draw the layout to.
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

    HashMultiMapLinkedList<Token, Edge> loops = new HashMultiMapLinkedList<Token, Edge>();
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
    HashMultiMapLinkedList<Edge, Edge>
      dominates = new HashMultiMapLinkedList<Edge, Edge>();

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
    HashMultiMapLinkedList<Token, Edge> vertex2edges = new HashMultiMapLinkedList<Token, Edge>();
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
   * The size of the arrow.
   *
   * @param arrowSize the size of the arrow.
   */
  public void setArrowSize(int arrowSize) {
    this.arrowSize = arrowSize;
  }

  /**
   * Return the arrow size.
   *
   * @return the size of the arrow.
   */
  public int getArrowSize() {
    return arrowSize;
  }

}
