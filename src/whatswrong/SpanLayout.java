package whatswrong;


import javautils.Counter;
import javautils.HashMultiMapList;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

/**
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class SpanLayout implements EdgeLayout {

  private int baseline = 1;
  private int heightPerLevel = 15;
  private int arrowSize = 2;
  private int vertexExtraSpace = 12;
  private boolean curve = true;
  private boolean revert = true;
  private boolean separationLines = true;

  private HashMap<String, Color> colors = new HashMap<String, Color>();
  private HashMap<String, BasicStroke> strokes = new HashMap<String, BasicStroke>();
  private BasicStroke defaultStroke = new BasicStroke();
  private HashMap<Edge, Point> from;
  private HashMap<Edge, Point> to;
  private HashMap<Shape, Edge> shapes = new HashMap<Shape, Edge>();
  private HashSet<Edge> selected = new HashSet<Edge>();
  private HashSet<Edge> visible = new HashSet<Edge>();

  private HashMap<String, Integer> orders = new HashMap<String, Integer>();

  private int maxHeight;
  private int maxWidth;
  private double totalTextMargin = 6.0;

  public void setColor(String type, Color color) {
    colors.put(type, color);
  }

  public void setStroke(String type, BasicStroke stroke) {
    strokes.put(type, stroke);
  }

  public void setTypeOrder(String type, int order) {
    orders.put(type, order);
  }

  public int getOrder(String type) {
    Integer order = orders.get(type);
    return order == null ? Integer.MIN_VALUE : order;
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

  public boolean isSeparationLines() {
    return separationLines;
  }

  public void setSeparationLines(boolean separationLines) {
    this.separationLines = separationLines;
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

  public Map<TokenVertex, Integer> estimateRequiredTokenWidths(
    Collection<Edge> edges, Graphics2D g2d) {

    HashMap<TokenVertex, Integer> result = new HashMap<TokenVertex, Integer>();
    for (Edge edge : edges) {
      if (edge.getFrom() == edge.getTo()) {
        Font font = new Font(g2d.getFont().getName(), Font.PLAIN, 8);
        FontRenderContext frc = g2d.getFontRenderContext();
        TextLayout layout = new TextLayout(edge.getLabel(), font, frc);
        Integer oldWidth = result.get(edge.getFrom());
        int width = oldWidth == null ? (int) layout.getBounds().getWidth() :
          (int) Math.max(layout.getBounds().getWidth(), oldWidth);
        result.put(edge.getFrom(), (int) (width + totalTextMargin));
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
        int orderOver = getOrder(over.getTypePrefix());
        int orderUnder = getOrder(under.getTypePrefix());
        if (orderOver > orderUnder ||
          orderOver == orderUnder && (
            over.covers(under) ||
              over.coversSemi(under) ||
              over.coversExactly(under) && over.lexicographicOrder(under) > 0 ||
              over.overlaps(under) && over.getMinIndex() < under.getMinIndex()
          ))
          dominates.add(over, under);

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

      if (maxX - minX < layout.getBounds().getWidth() + totalTextMargin) {
        double middle = minX + (maxX - minX) / 2.0;
        double textWidth = layout.getBounds().getWidth() + totalTextMargin;
        minX = (int) (middle - textWidth / 2.0);
        maxX = (int) (middle + textWidth / 2.0);
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
      int labely = height + heightPerLevel / 2;
      layout.draw(g2d, labelx, labely);
      g2d.setColor(old);
      //Area area = new Area();
      //area.add(shape);
      //shape.append(layout.getOutline(null), false);
      Rectangle2D labelBounds = layout.getBounds();
      shapes.put(shape, edge);
      //shapes.put(new Rectangle.Double(labelx,labely,labelBounds.getWidth(), labelBounds.getHeight()), edge);

    }

    if (separationLines) {
      //find largest depth for each prefix type
      HashMap<String, Integer> minDepths = new HashMap<String, Integer>();
      for (Edge e : edges) {
        int edgeDepth = depth.get(e);
        Integer typeDepth = minDepths.get(e.getTypePrefix());
        if (typeDepth == null || typeDepth > edgeDepth) {
          typeDepth = edgeDepth;
          minDepths.put(e.getTypePrefix(), typeDepth);
        }
      }
      for (Integer d : minDepths.values()) {
        double height = ((!revert ? maxDepth - d : d) * heightPerLevel);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(0, (int) height, getWidth(), (int) height);

      }
    }

    //draw separation lines


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