package whatswrong;


import javautils.HashMultiMapList;
import javautils.Counter;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

/**
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class SpanLayout extends DependencyLayout implements EdgeLayout {

  private int baseline = -1;
  private int heightPerLevel = 15;
  private int arrowSize = 2;
  private int vertexExtraSpace = 12;
  private boolean curve = true;
  private boolean revert = true;

  private HashMap<String, Color> colors = new HashMap<String, Color>();
  private HashMap<String, BasicStroke> strokes = new HashMap<String, BasicStroke>();
  private BasicStroke defaultStroke = new BasicStroke();
  private HashMap<Edge, Point> from;
  private HashMap<Edge, Point> to;
  private HashMap<Shape, Edge> shapes = new HashMap<Shape, Edge>();
  private HashSet<Edge> selected = new HashSet<Edge>();
  private HashSet<Edge> visible = new HashSet<Edge>();


  private int maxHeight;
  private int maxWidth;
  private double totalTextMargin = 6.0;

  public void setColor(String type, Color color) {
    colors.put(type, color);
  }

  public void setStroke(String type, BasicStroke stroke) {
    strokes.put(type, stroke);
  }

  public BasicStroke getStroke(Edge edge) {
    BasicStroke stroke = getStroke(edge.getType());
    return (selected.contains(edge)) ?
      new BasicStroke(stroke.getLineWidth() + 1.5f, stroke.getEndCap(), stroke.getLineJoin(),
        stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase()) :
      stroke;
  }

  public BasicStroke getStroke(String type) {
    for (String substring : strokes.keySet())
      if (type.contains(substring)) {
        return strokes.get(substring);
      }
    return defaultStroke;
  }

  public Color getColor(String type) {
    for (String substring : colors.keySet())
      if (type.contains(substring)) return colors.get(substring);
    return Color.BLACK;
  }

  public void addToSelection(Edge edge) {
    selected.add(edge);
  }

  public void removeFromSelection(Edge edge) {
    selected.remove(edge);
  }

  public void clearSelection() {
    selected.clear();
  }

  public void onlyShow(Collection<Edge> edges) {
    this.visible.clear();
    this.visible.addAll(edges);
  }

  public void showAll() {
    visible.clear();
  }

  public void toggleSelection(Edge edge) {
    if (selected.contains(edge)) selected.remove(edge);
    else selected.add(edge);
  }


  public Set<Edge> getSelected() {
    return Collections.unmodifiableSet(selected);
  }

  public void select(Edge edge) {
    selected.clear();
    selected.add(edge);
  }


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

  public void layout(Collection<Edge> edges, TokenLayout tokenLayout, Graphics2D g2d) {
    if (visible.size() > 0) {
      edges = new HashSet<Edge>(edges);
      edges.retainAll(visible);
    }

    //find out height of each edge
    shapes.clear();

    Counter<Edge> depth = new Counter<Edge>();
    Counter<Edge> offset = new Counter<Edge>();
    HashMultiMapList<Edge, Edge>
      dominates = new HashMultiMapList<Edge, Edge>();


    for (Edge over : edges)
      for (Edge under : edges) {
        if (over != under && (
          over.covers(under) || over.coversSemi(under) ||
          over.coversExactly(under) && over.lexicographicOrder(under) > 0)||
          over.overlaps(under) && over.getMinIndex() < under.getMinIndex() ) {
          dominates.add(over, under);
        }
      }

    for (Edge edge : edges)
      calculateDepth(dominates, depth, edge);

    //calculate maxHeight and maxWidth
    maxWidth = tokenLayout.getWidth() + 1;
    int maxDepth = depth.getMaximum();
    maxHeight = edges.size() > 0 ? (maxDepth + 1) * heightPerLevel + 3 : 1;
    //in case there are no edges that cover other edges (depth == 0) we need
    //to increase the height slightly because loops on the same token
    //have height of 1.5 levels

    //build map from vertex to incoming/outgoing edges
    HashMultiMapList<TokenVertex, Edge> vertex2edges = new HashMultiMapList<TokenVertex, Edge>();
    for (Edge edge : edges) {
      vertex2edges.add(edge.getFrom(), edge);
      vertex2edges.add(edge.getTo(), edge);
    }
    //assign starting and end points of edges by sorting the edges per vertex
    from = new HashMap<Edge, Point>();
    to = new HashMap<Edge, Point>();

    //draw each edge
    for (Edge edge : edges) {
      //set Color and remember old color
      Color old = g2d.getColor();
      g2d.setColor(getColor(edge.getType()));

      //prepare label (will be needed for spacing)
      Font font = new Font(g2d.getFont().getName(), Font.PLAIN, 8);
      FontRenderContext frc = g2d.getFontRenderContext();
      TextLayout layout = new TextLayout(edge.getLabel(), font, frc);


      //draw lines
      Integer spanLevel = revert ? maxDepth - depth.get(edge) : depth.get(edge);
      int height = baseline + maxHeight - (spanLevel + 1) * heightPerLevel + offset.get(edge);
      g2d.setStroke(getStroke(edge));

      int buffer = 2;

      Rectangle2D fromBounds = tokenLayout.getBounds(edge.getFrom());
      Rectangle2D toBounds = tokenLayout.getBounds(edge.getTo());
      int minX = (int) Math.min(fromBounds.getMinX(), toBounds.getMinX());
      int maxX = (int) Math.max(fromBounds.getMaxX(), toBounds.getMaxX());

      if (maxX - minX < layout.getBounds().getWidth() + totalTextMargin){
        double middle = minX + (maxX - minX)/2.0;
        double textWidth = layout.getBounds().getWidth() + totalTextMargin;
        minX = (int)(middle - textWidth / 2.0);
        maxX = (int)(middle + textWidth / 2.0);
      }

      //connection
      //GeneralPath shape = createRectArrow(p1, p2, p3, p4);
      Shape shape = curve ?
        new RoundRectangle2D.Double(minX, height - buffer, maxX - minX, heightPerLevel - 2 * buffer, 8, 8) :
        new Rectangle2D.Double(minX, height - buffer, maxX - minX, heightPerLevel - 2 * buffer);

      //GeneralPath shape = createCurveArrow(curveLength, p1, p2, p3, p4);
      g2d.draw(shape);

      //write label in the middle under
      int labelx = minX + (maxX - minX) / 2 - (int) layout.getBounds().getWidth() / 2;
      int labely = (int) (height + heightPerLevel/2);
      layout.draw(g2d, labelx, labely);
      g2d.setColor(old);
      //Area area = new Area();
      //area.add(shape);
      //shape.append(layout.getOutline(null), false);
      Rectangle2D labelBounds = layout.getBounds();
      shapes.put(shape, edge);
      //shapes.put(new Rectangle.Double(labelx,labely,labelBounds.getWidth(), labelBounds.getHeight()), edge);


    }


  }


  private GeneralPath createRectB(Point p1, Point p2, Point p3, Point p4) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(p1.x, p1.y);
    shape.lineTo(p2.x, p2.y);
    shape.lineTo(p3.x, p3.y);
    shape.lineTo(p4.x, p4.y);
    return shape;
  }

  private GeneralPath createCurveArrow(Point p1, Point p2, Point p3, Point p4) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(p1.x, p1.y);
    shape.curveTo(p2.x, p2.y, p2.x, p2.y, p2.x + (p3.x - p2.x) / 2, p2.y);
    shape.curveTo(p3.x, p3.y, p3.x, p3.y, p4.x, p4.y);
    shape.moveTo(p3.x, p3.y);
    shape.closePath();
    return shape;
  }

  private GeneralPath createCurveArrow(int curveLength, Point p1, Point p2, Point p3, Point p4) {
    GeneralPath shape = new GeneralPath();
    Point c1 = p1;
    Point c2 = new Point(p2.x, p2.y + curveLength);
    Point c3 = new Point(p2.x + (p3.x > p2.x ? curveLength : -curveLength), p2.y);
    Point c4 = new Point(p3.x - (p3.x > p2.x ? curveLength : -curveLength), p2.y);
    Point c5 = new Point(p3.x, p3.y + curveLength);
    Point c6 = p4;

//    System.out.println("c1 = " + c1);
//    System.out.println("c2 = " + c2);
//    System.out.println("c3 = " + c3);
//    System.out.println("c4 = " + c4);
//    System.out.println("c5 = " + c5);
//    System.out.println("c6 = " + c6);

    shape.moveTo(c1.x, c1.y);
    shape.lineTo(c2.x, c2.y);
    shape.curveTo(p2.x, p2.y, p2.x, p2.y, c3.x, c3.y);
    shape.lineTo(c4.x, c4.y);
    shape.curveTo(p3.x, p3.y, p3.x, p3.y, c5.x, c5.y);
    shape.lineTo(c6.x, c6.y);
    return shape;
  }


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

  public Point getFrom(Edge edge) {
    return from.get(edge);
  }

  public Point getTo(Edge edge) {
    return to.get(edge);
  }

  public int getHeight() {
    return maxHeight;
  }

  public int getWidth() {
    return maxWidth;
  }

  public TextLayout getLabel() {
    return null;
  }


  public int getHeightPerLevel() {
    return heightPerLevel;
  }


  public boolean isCurve() {
    return curve;
  }

  public void setCurve(boolean curve) {
    this.curve = curve;
  }

  public void setBaseline(int baseline) {
    this.baseline = baseline;
  }

  public void setHeightPerLevel(int heightPerLevel) {
    this.heightPerLevel = heightPerLevel;
  }

  public void setArrowSize(int arrowSize) {
    this.arrowSize = arrowSize;
  }

  public void setVertexExtraSpace(int vertexExtraSpace) {
    this.vertexExtraSpace = vertexExtraSpace;
  }

  public int getVertexExtraSpace() {
    return vertexExtraSpace;
  }

  public int getArrowSize() {
    return arrowSize;
  }

  public int getBaseline() {
    return baseline;
  }
}